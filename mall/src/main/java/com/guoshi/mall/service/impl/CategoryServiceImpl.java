package com.guoshi.mall.service.impl;

import com.guoshi.mall.consts.MallConst;
import com.guoshi.mall.dao.CategoryMapper;
import com.guoshi.mall.pojo.Category;
import com.guoshi.mall.service.ICategoryService;
import com.guoshi.mall.vo.CategoryVo;
import com.guoshi.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ResponseVo<List<CategoryVo>> selectAll() {
        List<Category> categories = categoryMapper.selectAll();

//        List<CategoryVo> categoryVoList = new ArrayList<>();
//        for (Category category : categories) {
//            if (category.getParentId().equals(MallConst.ROOT_PARENT_ID)) {
//                CategoryVo categoryVo = new CategoryVo();
//                BeanUtils.copyProperties(category, categoryVo);
//                categoryVoList.add(categoryVo);
//            }
//        }

        // 查询一级目录
        // lambda 表达式写法 + stream
        List<CategoryVo> categoryVoList = categories.stream()
                .filter(e -> e.getParentId().equals(MallConst.ROOT_PARENT_ID))
                .map(this::categoryToCategoryVo)
                .sorted(Comparator.comparing(CategoryVo::getSortOrder).reversed())
                .collect(Collectors.toList());

        // 查询子目录
        findSubCategory(categoryVoList, categories);

        return ResponseVo.success(categoryVoList);
    }

    @Override
    public void findSubCategoryId(Integer id, Set<Integer> resultSet) {
        List<Category> categories = categoryMapper.selectAll();
        for (Category category : categories) {
            if (category.getParentId().equals(id)) {
                resultSet.add(category.getId());

                findSubCategoryId(category.getId(), resultSet, categories);
            }
        }
    }

    public void findSubCategoryId(Integer id, Set<Integer> resultSet, List<Category> categories) {
        for (Category category : categories) {
            if (category.getParentId().equals(id)) {
                resultSet.add(category.getId());

                findSubCategoryId(category.getId(), resultSet);
            }
        }
    }

    /**
     * Category对象 转化为 CategoryVo对象
     *
     * @param category
     * @return
     */
    private CategoryVo categoryToCategoryVo(Category category) {
        CategoryVo categoryVo = new CategoryVo();
        BeanUtils.copyProperties(category, categoryVo);
        return categoryVo;
    }

    /**
     * 查询类别子目录
     *
     * @param categoryVoList
     * @param categories
     */
    private void findSubCategory(List<CategoryVo> categoryVoList, List<Category> categories) {
        for (CategoryVo categoryVo : categoryVoList) {
            List<CategoryVo> subCategoryVoList = new ArrayList<>();

            for (Category category : categories) {
                if (categoryVo.getId().equals(category.getParentId())) {
                    CategoryVo subCategoryVo = categoryToCategoryVo(category);
                    subCategoryVoList.add(subCategoryVo);
                }
            }

            // 排序
            subCategoryVoList.sort(Comparator.comparing(CategoryVo::getSortOrder).reversed());
            categoryVo.setSubCategories(subCategoryVoList);
            if (subCategoryVoList.size() > 0) {
                findSubCategory(subCategoryVoList, categories);
            }
        }
    }

}
