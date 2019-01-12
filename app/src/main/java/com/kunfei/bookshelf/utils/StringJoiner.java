package com.kunfei.bookshelf.utils;

import java.util.Objects;

public class StringJoiner {
    private String emptyValue;
    // 前缀
    private final String prefix;
    // 分隔符
    private final String delimiter;
    // 后缀
    private final String suffix;
    // 值
    private StringBuilder value;

    /**
     * 构造器
     */
    public StringJoiner(CharSequence delimiter) {
        this(delimiter, "", "");
    }

    public StringJoiner(CharSequence delimiter,
                        CharSequence prefix,
                        CharSequence suffix) {
        Objects.requireNonNull(prefix, "The prefix must not be null");
        Objects.requireNonNull(delimiter, "The delimiter must not be null");
        Objects.requireNonNull(suffix, "The suffix must not be null");
        // make defensive copies of arguments
        this.prefix = prefix.toString();
        this.delimiter = delimiter.toString();
        this.suffix = suffix.toString();
        this.emptyValue = this.prefix + this.suffix;
    }

    // 拼接
    public StringJoiner add(CharSequence newElement) {
        prepareBuilder().append(newElement);
        return this;
    }

    // 预拼接value
    private StringBuilder prepareBuilder() {
        // value已加前缀
        if (value != null) {
            // 此时添加分隔符
            value.append(delimiter);
        } else {
            // value未加前缀时需要先添加前缀
            value = new StringBuilder().append(prefix);
        }
        return value;
    }

    //重写了toString 方法
    @Override
    public String toString() {
        if (value == null) {
            // value未进行任何字符拼接时反悔emptyValue
            return emptyValue;
        } else {
            // 后缀为""字符时，直接返回value
            if (suffix.equals("")) {
                return value.toString();
            } else {
                // 获取value未拼接后缀的长度
                int initialLength = value.length();
                String result = value.append(suffix).toString();
                // reset value to pre-append initialLength
                // 此处是为了保证value.toString()为未拼接后缀前的字符串
                value.setLength(initialLength);
                return result;
            }
        }
    }
}
