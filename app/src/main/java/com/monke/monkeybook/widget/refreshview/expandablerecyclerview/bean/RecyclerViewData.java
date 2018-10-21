package com.monke.monkeybook.widget.refreshview.expandablerecyclerview.bean;

import java.util.List;

/**
 * author：Drawthink
 * describe:
 * date: 2017/5/22
 * T 为group数据对象
 * S 为child数据对象
 */

public class RecyclerViewData<T, S> {

    private GroupItem groupItem;

    /**
     * @param groupData
     * @param childDatas
     * @param isExpand   初始化展示数据时，该组数据是否展开
     */
    public RecyclerViewData(T groupData, List<S> childDatas, boolean isExpand) {
        this.groupItem = new GroupItem(groupData, childDatas, isExpand);
    }

    public RecyclerViewData(T groupData, List<S> childDatas) {
        this.groupItem = new GroupItem(groupData, childDatas, false);
    }

    public GroupItem getGroupItem() {
        return groupItem;
    }

    public void setGroupItem(GroupItem groupItem) {
        this.groupItem = groupItem;
    }

    public T getGroupData() {
        return (T) groupItem.getGroupData();
    }

    public void removeChild(int position) {
        if (null == groupItem || !groupItem.hasChilds()) {
            return;
        }
        groupItem.getChildDatas().remove(position);
    }

    public S getChild(int childPosition) {
        return (S) groupItem.getChildDatas().get(childPosition);
    }

}
