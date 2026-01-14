package edu.univ.erp.domain;

public class GradeComponent {
    private int componentId;
    private int sectionId;
    private String componentName;
    private double weight;
    private double maxScore;

    public GradeComponent(int componentId, int sectionId, String componentName, double weight, double maxScore) {
        this.componentId = componentId;
        this.sectionId = sectionId;
        this.componentName = componentName;
        this.weight = weight;
        this.maxScore = maxScore;
    }

    public int getComponentId() { return componentId; }
    public int getSectionId() { return sectionId; }
    public String getComponentName() { return componentName; }
    public double getWeight() { return weight; }
    public double getMaxScore() { return maxScore; }
}