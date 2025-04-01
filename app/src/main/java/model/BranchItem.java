package model;

import java.io.Serializable;

public class BranchItem implements Serializable {

    private int id;
    private String branch_name;
    private boolean isSelected;

    public BranchItem(int id, String branch_name, boolean isSelected) {
        this.id = id;
        this.branch_name = branch_name;
        this.isSelected = isSelected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBranch_name() {
        return branch_name;
    }

    public void setBranch_name(String branch_name) {
        this.branch_name = branch_name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
