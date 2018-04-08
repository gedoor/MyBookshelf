package com.monke.monkeybook.bean;

import java.util.List;

public class FindKindGroupBean {
    private String groupName;
    private int childrenCount;
    private List<FindKindBean> children;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public List<FindKindBean> getChildren() {
        return children;
    }

    public void setChildren(List<FindKindBean> children) {
        this.children = children;
    }
}
