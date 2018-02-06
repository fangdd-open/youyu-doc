package com.fangdd.tp.controller.web;

import com.fangdd.tp.doclet.pojo.*;
import com.fangdd.tp.doclet.render.EntityHandle;
import com.fangdd.tp.doclet.render.markdown.DubboApiMarkdownRender;
import com.fangdd.tp.doclet.render.markdown.EntityJsonMarkdownRender;
import com.fangdd.tp.dto.request.DocQuery;
import com.fangdd.tp.service.DocService;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * @auth ycoe
 * @date 18/1/24
 */
@Controller
@RequestMapping
public class DocWebController extends BaseWebController {
    @Autowired
    private DocService docService;

    @RequestMapping({"/index.html", ""})
    public ModelAndView index(DocQuery query) {
        List<Artifact> docs = docService.getDocList(query);
        addData("docs", docs);
        return view("index");
    }

    @RequestMapping("/doc/{docId}/")
    public ModelAndView doc(
            @PathVariable String docId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long version
    ) {
        DocDto doc = docService.get(docId, version);

        addData("code", code == null ? "" : code);
        if (!Strings.isNullOrEmpty(code)) {
            Api apiData = getApi(code, doc);
            if (apiData != null) {
                loadApiData(doc, apiData);
            }

        }

        addData("artifact", doc.getArtifact());
        addData("chapters", doc.getChapters());
        Map<String, Entity> entityMap = Maps.newHashMap();
        doc.getEntities().forEach(entity -> entityMap.put(entity.getName(), entity));
        addData("entityMap", entityMap);

        return view("doc");
    }

    private void loadApiData(DocDto doc, Api apiData) {
        addData("api", apiData);

        doc.getEntities().forEach(EntityHandle::addEntity);
        String response = EntityJsonMarkdownRender.render(apiData.getResponse());
        addData("response", response);

        if(apiData.getType() != null && apiData.getType() == 1) {
            //Dubbo接口
            String dubboApi = DubboApiMarkdownRender.getDubboApi(apiData);
            addData("dubboApi", dubboApi);

            String code = apiData.getCode();
            int index = code.lastIndexOf('.');
            code = code.substring(0, index);
            index = code.lastIndexOf('.');
            code += " " + code.substring(index + 1, index + 2).toLowerCase() + code.substring(index + 2);
            addData("icoCode", code);
        }
    }

    private Api getApi(@RequestParam(required = false) String code, DocDto doc) {
        for (Chapter chapter : doc.getChapters()) {
            for (Section section : chapter.getSections()) {
                for (Api api : section.getApis()) {
                    if (api.getCode().equals(code)) {
                        return api;
                    }
                }
            }
        }
        return null;
    }
}