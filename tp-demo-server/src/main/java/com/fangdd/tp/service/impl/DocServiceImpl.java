package com.fangdd.tp.service.impl;

import com.fangdd.tp.dao.DocChapterDao;
import com.fangdd.tp.dao.DocDao;
import com.fangdd.tp.dao.DocEntityDao;
import com.fangdd.tp.dao.DocLogDao;
import com.fangdd.tp.doclet.pojo.Artifact;
import com.fangdd.tp.doclet.pojo.Chapter;
import com.fangdd.tp.doclet.pojo.DocDto;
import com.fangdd.tp.doclet.pojo.Entity;
import com.fangdd.tp.doclet.pojo.entity.DocLog;
import com.fangdd.tp.dto.request.DocLogQuery;
import com.fangdd.tp.dto.request.DocQuery;
import com.fangdd.tp.service.DocService;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.Block;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @auth ycoe
 * @date 18/1/23
 */
@Service
public class DocServiceImpl implements DocService {
    private static final String DOC_VERSION = "docVersion";
    private static final String DOC_ID = "docId";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String ARTIFACT_ID = "artifactId";

    @Autowired
    private DocDao docDao;

    @Autowired
    private DocEntityDao docEntityDao;

    @Autowired
    private DocLogDao docLogDao;

    @Autowired
    private DocChapterDao docChapterDao;

    public DocDto get(String id, Long version) {
        DocDto docDto = new DocDto();

        Artifact doc;
        if (version != null && version > 0) {
            //有指定版本号，从历史变更中查询
            doc = docLogDao.getEntity(Filters.and(
                    Filters.eq(DOC_ID, id),
                    Filters.eq(DOC_VERSION, version)
            ));
        } else {
            doc = docDao.getEntityById(id);
        }

        List<Chapter> chapters = Lists.newArrayList();
        Bson versionFilter = Filters.and(
                Filters.eq(DOC_ID, id),
                Filters.eq(DOC_VERSION, version != null && version > 0 ? version : doc.getDocVersion())
        );
        docChapterDao
                .find(versionFilter)
                .forEach((Block<? super Chapter>) chapters::add);

        List<Entity> entities = Lists.newArrayList();
        docEntityDao
                .find(versionFilter)
                .forEach((Block<? super Entity>) entities::add);

        docDto.setArtifact(doc);
        docDto.setChapters(chapters);
        docDto.setEntities(entities);
        return docDto;
    }

    @Override
    public List<DocLog> getDocLogList(DocLogQuery query) {
        return docLogDao
                .find(Filters.eq(DOC_ID, query.getDocId()))
                .skip((query.getPageNo() - 1) * query.getPageSize())
                .limit(query.getPageSize())
                .into(Lists.newArrayList());
    }

    @Override
    public List<Artifact> getDocList(DocQuery query) {
        String keyword = query.getKeyword();
        Bson filter;
        if (!Strings.isNullOrEmpty(keyword)) {
            //有关键字搜索时
            filter = Filters.or(
                    Lists.newArrayList(
                            Filters.regex(DOC_ID, keyword),
                            Filters.regex(NAME, keyword),
                            Filters.regex(DESCRIPTION, keyword),
                            Filters.regex(ARTIFACT_ID, keyword)
                    )
            );
        } else {
            filter = new Document();
        }
        return docDao
                .find(filter)
                .skip((query.getPageNo() - 1) * query.getPageSize())
                .limit(query.getPageSize())
                .into(Lists.newArrayList());
    }
}