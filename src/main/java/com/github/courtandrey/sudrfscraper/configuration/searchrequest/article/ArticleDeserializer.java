package com.github.courtandrey.sudrfscraper.configuration.searchrequest.article;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.courtandrey.sudrfscraper.service.LawBookHelper;

import java.io.IOException;
import java.util.Objects;

public class ArticleDeserializer extends JsonDeserializer<Article> {

    @Override
    public Article deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode t = codec.readTree(jsonParser);
        String articleClass = t.get("field").asText();
        try {
            if ("CRIMINAL".equals(articleClass)) {
                String article = t.get("article").asText("");
                String subArticle = t.get("subArticle").asText("");
                String part = t.get("part").asText("");
                String letter = t.get("letter").asText("");
                boolean headLess = Objects.equals(article, "");
                if (headLess && part.length() > 0) return null;
                if (headLess && subArticle.length() > 0) return null;
                if (headLess && letter.length() > 0) return null;
                if (letter.length() > 1) return null;
                if (headLess) return new CriminalArticle();
                CriminalArticle criminalArticle = new CriminalArticle();
                criminalArticle.setArticle(Integer.parseInt(article));
                if (subArticle.length() > 0) {
                    criminalArticle.setSubArticle(Integer.parseInt(subArticle));
                }
                if (part.length() > 0) {
                    criminalArticle.setPart(Integer.parseInt(part));
                }
                if (letter.length() > 0) {
                    criminalArticle.setLetter(letter.charAt(0));
                }
                return criminalArticle;

            } else if ("CAS".equals(articleClass)) {
                String partOfCas = t.get("partOfCas").asText("");
                String mosgorsudCode = t.get("mosgorsudCode").asText("");
                if (LawBookHelper.getMosGorSudCodeCas(mosgorsudCode) == null) {
                    return null;
                }
                if (partOfCas.equals("") && Objects.equals(mosgorsudCode, "")) return new CASArticle();
                else if (Objects.equals(mosgorsudCode, "")) return null;
                else if (partOfCas.equals("")) return null;
                return new CASArticle(partOfCas, mosgorsudCode);
            } else if ("ADMIN".equals(articleClass)) {
                String chapter = t.get("chapter").asText("");
                String article = t.get("article").asText("");
                String subArticle = t.get("subArticle").asText("");
                String part = t.get("part").asText("");
                String subPart = t.get("subPart").asText("");
                boolean headLess = false;

                if (chapter.length() == 0 && article.length() == 0) headLess = true;
                else if (article.length() == 0) return null;
                else if (chapter.length() == 0) return null;

                if (headLess && part.length() > 0) return null;
                if (headLess && subPart.length() > 0) return null;
                if (headLess && subArticle.length() > 0) return null;
                if (headLess) return new AdminArticle();
                AdminArticle adminArticle = new AdminArticle();
                adminArticle.setChapter(Integer.parseInt(chapter));
                adminArticle.setArticle(Integer.parseInt(article));
                if (subArticle.length() > 0) {
                    adminArticle.setSubArticle(Integer.parseInt(subArticle));
                }
                if (part.length() > 0) {
                    adminArticle.setPart(Integer.parseInt(part));
                }
                if (subPart.length() > 0) {
                    adminArticle.setSubPart(Integer.parseInt(subPart));
                }
                return adminArticle;
            }
            else if (articleClass.equals("MATERIAL_PROCEEDING")) {
                String partOfUPK = t.get("partOfUPK").asText("");
                String mosgorsudCode = t.get("mosgorsudCode").asText("");
                if (partOfUPK.equals("") && Objects.equals(mosgorsudCode, "")) return new MaterialProceedingArticle();
                else if (Objects.equals(mosgorsudCode, "")) return null;
                else if (partOfUPK.equals("")) return null;
                return new MaterialProceedingArticle(partOfUPK, mosgorsudCode);
            }
        } catch (Exception ignored) {}

        return null;
    }
}
