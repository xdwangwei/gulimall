package com.vivi.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wangwei
 * 2020/10/24 22:27
 *
 * 商城首页，实现鼠标放在一级分类上，自动显示出对应的二级三级分类，所需要的数据模型
 *
 * 最终需要一个 Map<id, List<Catelog2VO>>，map里面的键是catelog1Id，也就是一级分类的id
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catelog2VO {

    private String catelog1Id;

    private List<Catelog3VO> catelog3List;

    private String id;

    private String name;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catelog3VO {

        private String catelog2Id;

        private String id;

        private String name;
    }
}
