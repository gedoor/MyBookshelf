package com.kunfei.bookshelf.widget.recycler.expandable;

import android.view.View;

/**
 * author：Drawthink
 * describe：RecyclerViewListener
 * date: 2017/5/22
 */

public interface OnRecyclerViewListener {

    /**
     * 单击事件
     */
    interface OnItemClickListener {
        /**
         * position 当前在列表中的position
         */
        void onGroupItemClick(int position, int groupPosition, View view);

        void onChildItemClick(int position, int groupPosition, int childPosition, View view);
    }

    /**
     * 长按事件
     */
    interface OnItemLongClickListener {
        void onGroupItemLongClick(int position, int groupPosition, View view);

        void onChildItemLongClick(int position, int groupPosition, int childPosition, View view);
    }

    interface OnGroupExpandedListener {
        /**
         * 分组展开
         *
         * @param groupPosition 分组的位置
         */
        void onGroupExpanded(int groupPosition);
    }

    interface OnGroupCollapseListener {
        /**
         * 分组收起
         *
         * @param groupPosition 分组的位置
         */
        void onGroupCollapse(int groupPosition);
    }

}
