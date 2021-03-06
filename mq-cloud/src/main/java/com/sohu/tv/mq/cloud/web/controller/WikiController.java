package com.sohu.tv.mq.cloud.web.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sohu.tv.mq.cloud.util.MQCloudConfigHelper;
import com.sohu.tv.mq.cloud.util.Result;
import com.sohu.tv.mq.util.Version;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;

/**
 * wiki
 * 
 * @author yongfeigao
 * @date 2019年6月10日
 */
@Controller
@RequestMapping("/wiki")
public class WikiController {

    @Autowired
    private MQCloudConfigHelper mqCloudConfigHelper;

    @RequestMapping("/{path}/{filename}")
    public String subPages(@PathVariable String path, @PathVariable String filename,
            Map<String, Object> map) throws Exception {
        String html = markdown2html(path + "/" + filename, ".md");
        html = html.replace("${clientArtifactId}", mqCloudConfigHelper.getClientArtifactId());
        html = html.replace("${version}", Version.get());
        html = html.replace("${nexusDomain}", mqCloudConfigHelper.getNexusDomain());
        html = html.replace("${producer}", mqCloudConfigHelper.getProducerClass());
        html = html.replace("${consumer}", mqCloudConfigHelper.getConsumerClass());
        Result.setResult(map, html);
        
        // toc
        String toc = markdown2html(path + "/" + filename, ".toc.md");
        if(toc != null) {
            map.put("toc", toc);
        }
        return "wikiTemplate";
    }

    private String markdown2html(String filename, String suffix) throws Exception {
        String templatePath = "static/wiki/" + filename + suffix;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        if(inputStream == null) {
            return null;
        }
        String markdown = new String(read(inputStream));
        Document document = Parser.builder().build().parse(markdown);
        String html = HtmlRenderer.builder().build().render(document);
        return html;
    }
    
    private byte[] read(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            while((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } finally {
            if(bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {}
            }
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {}
            }
        }
        return bos.toByteArray();
    }

}
