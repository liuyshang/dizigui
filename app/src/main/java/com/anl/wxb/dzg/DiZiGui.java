package com.anl.wxb.dzg;

import java.util.List;

/**
 * Created by admin on 2015/9/1.
 */
public class DiZiGui {

    public List<Data> list;
    public String code;

    public class Data {
        public int id;
        public String pinyin;
        public String hanzi;
        public String jieshi;
    }

    public List<Data> getList() {
        return list;
    }

    public void setList(List<Data> list) {
        this.list = list;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}

