package com.kalaqia.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.kalaqia.pojo.Category;
import com.kalaqia.pojo.Product;
import com.kalaqia.service.CategoryService;
import com.kalaqia.service.ProductService;
import com.kalaqia.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("")
public class ProductController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;

    @RequestMapping("admin_product_add")
    /*获取Product对象，并插入到数据库*/
    public String add(Model model, Product p) {
        p.setCreateDate(new Date());
        productService.add(p);
        /*客户端跳转到admin_product_list,并带上参数cid*/
        return "redirect:admin_product_list?cid="+p.getCid();
    }

    @RequestMapping("admin_product_delete")
    /*获取id*/
    public String delete(int id) {
        /*根据id获取Product对象*/
        Product p = productService.get(id);
        /*借助productService删除这个对象对应的数据*/
        productService.delete(id);
        /*客户端跳转到admin_product_list，并带上参数cid*/
        return "redirect:admin_product_list?cid="+p.getCid();
    }

    @RequestMapping("admin_product_edit")
    public String edit(Model model, int id) {
        /*根据id获取product对象*/
        Product p = productService.get(id);
        /*根据product对象的cid产品获取Category对象，并把其设置在product对象的category产品上*/
        Category c = categoryService.get(p.getCid());
        p.setCategory(c);
        /*把product对象放在request的 "p" 产品中*/
        model.addAttribute("p", p);
        /*服务端跳转到admin/editProduct.jsp*/
        return "admin/editProduct";
    }

    @RequestMapping("admin_product_update")
    /*获取Product对象*/
    public String update(Product p) {
        /*借助productService更新这个对象到数据库*/
        productService.update(p);
        /*客户端跳转到admin_product_list，并带上参数cid*/
        return "redirect:admin_product_list?cid="+p.getCid();
    }

    @RequestMapping("admin_product_list")
    /*获取分类 cid,和分页对象page*/
    public String list(int cid, Model model, Page page) {
        Category c = categoryService.get(cid);
        /*通过PageHelper设置分页参数*/
        PageHelper.offsetPage(page.getStart(),page.getCount());
        /*基于cid，获取当前分类下的产品集合*/
        List<Product> ps = productService.list(cid);
        /*通过PageInfo获取产品总数*/
        int total = (int) new PageInfo<>(ps).getTotal();
        /*把总数设置给分页page对象*/
        page.setTotal(total);
        /*拼接字符串"&cid="+c.getId()，设置给page对象的Param值。
        因为产品分页都是基于当前分类下的分页，所以分页的时候需要传递这个cid*/
        page.setParam("&cid="+c.getId());
        /*把产品集合设置到 request的 "ps" 产品上*/
        model.addAttribute("ps", ps);
        /*把分类对象设置到 request的 "c" 产品上*/
        model.addAttribute("c", c);
        /*把分页对象设置到 request的 "page" 对象上*/
        model.addAttribute("page", page);
        /*服务端跳转到admin/listProduct.jsp页面*/
        return "admin/listProduct";
    }
}
