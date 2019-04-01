package com.kalaqia.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.kalaqia.pojo.Order;
import com.kalaqia.service.OrderItemService;
import com.kalaqia.service.OrderService;
import com.kalaqia.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("")
public class OrderController {
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;

    @RequestMapping("admin_order_list")
    /*获取分页对象*/
    public String list(Model model, Page page){
        PageHelper.offsetPage(page.getStart(),page.getCount());
        /*查询订单集合*/
        List<Order> os= orderService.list();
        /*获取订单总数并设置在分页对象上*/
        int total = (int) new PageInfo<>(os).getTotal();
        page.setTotal(total);
        /*借助orderItemService.fill()方法为这些订单填充上orderItems信息*/
        orderItemService.fill(os);
        /*把订单集合和分页对象设置在model上*/
        model.addAttribute("os", os);
        model.addAttribute("page", page);
        /*服务端跳转到admin/listOrder.jsp页面*/
        return "admin/listOrder";
    }

    @RequestMapping("admin_order_delivery")
    /*注入订单对象*/
    public String delivery(Order o) throws IOException {
        /*修改发货时间，设置发货状态*/
        o.setDeliveryDate(new Date());
        o.setStatus(OrderService.waitConfirm);
        /*更新到数据库*/
        orderService.update(o);
        /*客户端跳转到admin_order_list页面*/
        return "redirect:admin_order_list";
    }
}

