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
                if (headLess && !part.isEmpty()) return null;
                if (headLess && !subArticle.isEmpty()) return null;
                if (headLess && !letter.isEmpty()) return null;
                if (letter.length() > 1) return null;
                if (headLess) return new CriminalArticle();
                CriminalArticle criminalArticle = new CriminalArticle();
                criminalArticle.setArticle(Integer.parseInt(article));
                if (!subArticle.isEmpty()) {
                    criminalArticle.setSubArticle(Integer.parseInt(subArticle));
                }
                if (!part.isEmpty()) {
                    criminalArticle.setPart(Integer.parseInt(part));
                }
                if (!letter.isEmpty()) {
                    criminalArticle.setLetter(letter.charAt(0));
                }
                return criminalArticle;

            } else if ("CAS".equals(articleClass)) {
                String partOfCas = t.get("partOfCas").asText("");
                while (partOfCas.contains("  ")) {
                    partOfCas = partOfCas.replace("  ", " ");
                }
                String mosgorsudCode = t.get("mosgorsudCode").asText("");
                if (LawBookHelper.getMosGorSudCodeCas(mosgorsudCode) == null && !partOfCas.isEmpty()) {
                    return null;
                }
                if (partOfCas.isEmpty() && Objects.equals(mosgorsudCode, "")) return new CASArticle();
                else if (Objects.equals(mosgorsudCode, "")) return null;
                else if (partOfCas.isEmpty()) return null;
                return new CASArticle(partOfCas, mosgorsudCode);
            } else if ("ADMIN".equals(articleClass)) {
                String chapter = t.get("chapter").asText("");
                String article = t.get("article").asText("");
                String subArticle = t.get("subArticle").asText("");
                String part = t.get("part").asText("");
                String subPart = t.get("subPart").asText("");
                boolean headLess = false;

                if (chapter.isEmpty() && article.isEmpty()) headLess = true;
                else if (article.isEmpty()) return null;
                else if (chapter.isEmpty()) return null;

                if (headLess && !part.isEmpty()) return null;
                if (headLess && !subPart.isEmpty()) return null;
                if (headLess && !subArticle.isEmpty()) return null;
                if (headLess) return new AdminArticle();
                AdminArticle adminArticle = new AdminArticle();
                adminArticle.setChapter(Integer.parseInt(chapter));
                adminArticle.setArticle(Integer.parseInt(article));
                if (!subArticle.isEmpty()) {
                    adminArticle.setSubArticle(Integer.parseInt(subArticle));
                }
                if (!part.isEmpty()) {
                    adminArticle.setPart(Integer.parseInt(part));
                }
                if (!subPart.isEmpty()) {
                    adminArticle.setSubPart(Integer.parseInt(subPart));
                }
                return adminArticle;
            }
            else if (articleClass.equals("MATERIAL_PROCEEDING")) {
                String partOfUPK = t.get("partOfUPK").asText("");
                String mosgorsudCode = t.get("mosgorsudCode").asText("");
                if (partOfUPK.isEmpty() && Objects.equals(mosgorsudCode, "")) return new MaterialProceedingArticle();
                else if (Objects.equals(mosgorsudCode, "")) return null;
                else if (partOfUPK.isEmpty()) return null;
                return new MaterialProceedingArticle(partOfUPK, mosgorsudCode);
            }
            else if ("CIVIL".equals(articleClass)) {
                String partOfCas = t.get("partOfCas").asText("");
                while (partOfCas.contains("  ")) {
                    partOfCas = partOfCas.replace("  ", " ");
                }
                String mosgorsudCode = t.get("mosgorsudCode").asText("");
                if (partOfCas.isEmpty() && Objects.equals(mosgorsudCode, "")) return new CivilArticle();
                else if (Objects.equals(mosgorsudCode, "")) return null;
                else if (partOfCas.isEmpty()) return null;
                return new CivilArticle(partOfCas, mosgorsudCode);
            }
        } catch (Exception ignored) {}

        return null;
    }
}
