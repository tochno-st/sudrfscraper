package com.github.courtandrey.sudrfscraper.dump.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
@Getter
public class Case {
    @Setter
    private String CUI;
    private Long id;
    private int region;
    @Setter
    private String instance = "FIRST";
    private String name;
    private String caseNumber;
    private String entryDate;
    private String names;
    private String judge;
    private String resultDate;
    private String decision;
    private String endDate;
    private final List<Step> steps = new ArrayList<>();
    private final List<Side> sides = new ArrayList<>();
    @Setter
    private String consideredBy;
    private String text;
    @JsonIgnore
    @Setter
    private boolean isFormed = true;
    @JsonIgnore
    @Setter
    private String textUrl;
    @Getter
    private final HashMap<ConnectionType, String> connectedLinks = new HashMap<>();

    public void addConnectedLink(ConnectionType type, String link) {
        connectedLinks.put(type, link);
    }

    @Getter
    private final HashMap<LinkType, String> links = new HashMap<>();
    public static volatile AtomicLong idInteger = new AtomicLong(0);
    public boolean addStep(Step step) {
        return steps.add(step);
    }
    public boolean addSide(Side side) {
        return sides.add(side);
    }

    public Case() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public void setJudge(String judge) {
        this.judge = judge;
    }

    public void setResultDate(String resultDate) {
        this.resultDate = resultDate;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void addLink(LinkType link, String href) {
        links.put(link, href);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Case aCase = (Case) o;
        return region == aCase.region && Objects.equals(name, aCase.name) && Objects.equals(caseNumber, aCase.caseNumber) && Objects.equals(names, aCase.names) && Objects.equals(judge, aCase.judge) && Objects.equals(decision, aCase.decision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, name, caseNumber, names, judge, decision);
    }
}
