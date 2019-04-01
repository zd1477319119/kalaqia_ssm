package com.kalaqia.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.kalaqia.pojo.Category;
import com.kalaqia.service.CategoryService;
import com.kalaqia.util.ImageUtil;
import com.kalaqia.util.Page;
import com.kalaqia.util.UploadedImageFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/*注解@Controller声明当前类是一个控制器*/
@Controller
/*注解@RequestMapping("")表示访问的时候无需额外的地址*/
@RequestMapping("")
public class CategoryController {
    /*注解@Autowired把CategoryServiceImpl自动装配进了CategoryService 接口*/
    @Autowired
    CategoryService categoryService;


    /*分类管理列表*/    @RequestMapping("admin_category_list")
    public String list(Model model, Page page) {
        /*通过分页插件指定分页参数*/
        PageHelper.offsetPage(page.getStart(),page.getCount());
        /*调用list() 获取对应分页的数据*/
        List<Category> cs= categoryService.list();
        /*通过PageInfo获取总数*/
        int total = (int) new PageInfo<>(cs).getTotal();
        /*通过page.setTotal(total); 为分页对象设置总数*/
        page.setTotal(total);
        /*把分类集合放在"cs"中*/
        model.addAttribute("cs", cs);
        /*把分页对象放在 "page" 中*/
        model.addAttribute("page", page);
        /*根据springMVC.xml 配置文件，跳转到 WEB-INF/jsp/admin/listCategory.jsp 文件*/
        return "admin/listCategory";
    }

    /*新增分类*/
    @RequestMapping("admin_category_add")
    /*参数 Category c接受页面提交的分类名称,参数 session 用于在后续获取当前应用的路径，UploadedImageFile 用于接受上传的图片*/
    public String add(Category c, HttpSession session, UploadedImageFile uploadedImageFile) throws IOException {
        /*通过categoryService保存c对象*/
        categoryService.add(c);
        /*通过session获取ControllerContext,再通过getRealPath定位存放分类图片的路径。*/
        File imageFolder = new File(session.getServletContext().getRealPath("img/category"));
        /*根据分类id创建文件名*/
        File file = new File(imageFolder, c.getId() + ".jpg");
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        /*通过UploadedImageFile 把浏览器传递过来的图片保存在上述指定的位置*/
        uploadedImageFile.getImage().transferTo(file);
        /*通过ImageUtil.change2jpg(file); 确保图片格式一定是jpg，而不仅仅是后缀名是jpg.*/
        BufferedImage img = ImageUtil.change2jpg(file);
        ImageIO.write(img, "jpg", file);
        /*客户端跳转到admin_category_list*/
        return "redirect:/admin_category_list";
    }

    /*删除分类*/
    @RequestMapping("admin_category_delete")
    /*提供参数接受id注入，提供session参数，用于后续定位文件位置*/
    public String delete(int id,HttpSession session) throws IOException {
        /*通过categoryService删除数据*/
        categoryService.delete(id);
        /*通过session获取ControllerContext然后获取分类图片位置，接着删除分类图片*/
        File  imageFolder= new File(session.getServletContext().getRealPath("img/category"));
        File file = new File(imageFolder,id+".jpg");
        file.delete();
        /*客户端跳转到 admin_category_list*/
        return "redirect:/admin_category_list";
    }

    /*编辑分类*/
    @RequestMapping("admin_category_edit")
    /*参数id用来接受注入*/
    public String edit(int id,Model model)throws IOException{
        /*通过categoryService.get获取Category对象*/
        Category c = categoryService.get(id);
        /*把对象放在“c"上*/
        model.addAttribute("c",c);
        /*返回editCategory.jsp*/
        return "admin/editCategory";
    }

    /*编辑并修改分类*/
    @RequestMapping("admin_category_update")
    /*参数 Category c接受页面提交的分类名称，参数 session 用于在后续获取当前应用的路径，UploadedImageFile 用于接受上传的图片*/
    public String update(Category c, HttpSession session, UploadedImageFile uploadedImageFile) throws IOException{
        /*通过categoryService更新c对象*/
        categoryService.update(c);
        /*通过UploadedImageFile 把浏览器传递过来的图片保存在上述指定的位置*/
        MultipartFile image = uploadedImageFile.getImage();
        /*首先判断是否有上传图片，如果有上传，那么通过session获取ControllerContext,再通过getRealPath定位存放分类图片的路径。*/
        if(null!=image &&!image.isEmpty()){
            File  imageFolder= new File(session.getServletContext().getRealPath("img/category"));
            /*根据分类id创建文件名*/
            File file = new File(imageFolder,c.getId()+".jpg");
            image.transferTo(file);
            /*通过ImageUtil.change2jpg(file); 确保图片格式一定是jpg，而不仅仅是后缀名是jpg.*/
            BufferedImage img = ImageUtil.change2jpg(file);
            ImageIO.write(img, "jpg", file);
        }
        /*客户端跳转到admin_category_list*/
        return "redirect:/admin_category_list";
    }
}
