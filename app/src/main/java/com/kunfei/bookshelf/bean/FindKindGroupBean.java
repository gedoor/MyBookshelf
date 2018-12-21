package com.kunfei.bookshelf.bean;

import java.util.List;

public class FindKindGroupBean {
    private String groupName;
    private String groupTag;
    private int childrenCount;
    private List<FindKindBean> childrenList;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupTag() {
        return groupTag;
    }

    public void setGroupTag(String groupTag) {
        this.groupTag = groupTag;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public List<FindKindBean> getChildrenList() {
        return childrenList;
    }

    public void setChildrenList(List<FindKindBean> childrenList) {
        this.childrenList = childrenList;
    }
}
