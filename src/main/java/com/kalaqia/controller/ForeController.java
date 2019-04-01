package com.kalaqia.controller;


import com.github.pagehelper.PageHelper;
import com.kalaqia.pojo.*;
import com.kalaqia.service.*;
import comparator.*;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("")
public class ForeController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;

    /*home()方法映射首页访问路径 "forehome".*/
    @RequestMapping("forehome")
    public String home(Model model) {
        /*查询所有分类*/
        List<Category> cs= categoryService.list();
        /*为这些分类填充产品集合*/
        productService.fill(cs);
        /*为这些分类填充推荐产品集合*/
        productService.fillByRow(cs);
        model.addAttribute("cs", cs);
        /*服务端跳转到home.jsp*/
        return "fore/home";
    }

    @RequestMapping("foreregister")
    public String register(Model model,User user) {
        /*通过参数User获取浏览器提交的账号密码*/
        String name =  user.getName();
        /*通过HtmlUtils.htmlEscape(name);把账号里的特殊符号进行转义*/
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);
        /*判断用户名是否存在*/
        boolean exist = userService.isExist(name);

        /*如果已经存在，就服务端跳转到reigster.jsp，并且带上错误提示信息*/
        if(exist){
            String m ="用户名已经被使用,不能使用";
            model.addAttribute("msg", m);
            model.addAttribute("user", null);
            return "fore/register";
        }
        /*如果不存在，则加入到数据库中，并服务端跳转到registerSuccess.jsp页面*/
        userService.add(user);

        return "redirect:registerSuccessPage";
    }

    @RequestMapping("forelogin")
    /*获取账号密码*/
    public String login(@RequestParam("name") String name, @RequestParam("password") String password, Model model, HttpSession session) {
        /*把账号通过HtmlUtils.htmlEscape进行转义*/
        name = HtmlUtils.htmlEscape(name);
        /*根据账号和密码获取User对象*/
        User user = userService.get(name,password);
        /*如果对象为空，则服务端跳转回login.jsp，也带上错误信息，并且使用 loginPage.jsp 中的办法显示错误信息*/
        if(null==user){
            model.addAttribute("msg", "账号密码错误");
            return "fore/login";
        }
        /*如果对象存在，则把对象保存在session中，并客户端跳转到首页"forehome"*/
        session.setAttribute("user", user);
        return "redirect:forehome";
    }

    @RequestMapping("forelogout")
    public String logout( HttpSession session) {
        /*在session中去掉"user"*/
        session.removeAttribute("user");
        /*客户端跳转到首页:*/
        return "redirect:forehome";
    }

    @RequestMapping("foreproduct")
    /*获取参数pid*/
    public String product( int pid, Model model) {
        /*根据pid获取Product 对象p*/
        Product p = productService.get(pid);

        /*根据对象p，获取这个产品对应的单个图片集合*/
        List<ProductImage> productSingleImages = productImageService.list(p.getId(), ProductImageService.type_single);
        /*根据对象p，获取这个产品对应的详情图片集合*/
        List<ProductImage> productDetailImages = productImageService.list(p.getId(), ProductImageService.type_detail);
        p.setProductSingleImages(productSingleImages);
        p.setProductDetailImages(productDetailImages);

        /*获取产品的所有属性值*/
        List<PropertyValue> pvs = propertyValueService.list(p.getId());
        /*获取产品对应的所有的评价*/
        List<Review> reviews = reviewService.list(p.getId());
        /*设置产品的销量和评价数量*/
        productService.setSaleAndReviewNumber(p);
        /*把上述取值放在request属性上*/
        model.addAttribute("reviews", reviews);
        model.addAttribute("p", p);
        model.addAttribute("pvs", pvs);
        /*服务端跳转到 "product.jsp" 页面*/
        return "fore/product";
    }

    @RequestMapping("forecheckLogin")
    @ResponseBody
    public String checkLogin( HttpSession session) {
        /*获取session中的"user"对象*/
        User user =(User)  session.getAttribute("user");
        /*如果不为空，即表示已经登录，返回字符串"success"*/
        if(null!=user)
            return "success";
        /*如果为空，即表示未登录，返回字符串"fail"*/
        return "fail";
    }

    @RequestMapping("foreloginAjax")
    @ResponseBody
    public String loginAjax(@RequestParam("name") String name, @RequestParam("password") String password,HttpSession session) {
        /*获取账号密码*/
        name = HtmlUtils.htmlEscape(name);
        /*通过账号密码获取User对象*/
        User user = userService.get(name,password);

        /*如果User对象为空，那么就返回"fail"字符串。*/
        if(null==user)
            return "fail";
        /*如果User对象不为空，那么就把User对象放在session中，并返回"success" 字符串*/
        session.setAttribute("user", user);
        return "success";
    }

    @RequestMapping("forecategory")
    /*获取参数cid*/
    public String category(int cid,String sort, Model model) {
        /*根据cid获取分类Category对象 c*/
        Category c = categoryService.get(cid);
        /*为c填充产品*/
        productService.fill(c);
        /*为产品填充销量和评价数据*/
        productService.setSaleAndReviewNumber(c.getProducts());

        /*获取参数sort*/
        if(null!=sort){
            /*如果sort!=null，则根据sort的值，从5个Comparator比较器中选择一个对应的排序器进行排序*/
            switch(sort){
                case "review":
                    Collections.sort(c.getProducts(),new ProductReviewComparator());
                    break;
                case "date" :
                    Collections.sort(c.getProducts(),new ProductDateComparator());
                    break;

                case "saleCount" :
                    Collections.sort(c.getProducts(),new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(c.getProducts(),new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(c.getProducts(),new ProductAllComparator());
                    break;
            }
        }
        /*如果sort==null，即不排序*/
        /*把c放在model中*/
        model.addAttribute("c", c);
        /*服务端跳转到 category.jsp*/
        return "fore/category";
    }

    @RequestMapping("foresearch")
    /*获取参数keyword*/
    public String search( String keyword,Model model){

        /*根据keyword进行模糊查询，获取满足条件的前20个产品*/
        PageHelper.offsetPage(0,20);
        List<Product> ps= productService.search(keyword);
        /*为这些产品设置销量和评价数量*/
        productService.setSaleAndReviewNumber(ps);
        /*把产品结合设置在model的"ps"属性上*/
        model.addAttribute("ps",ps);
        /*服务端跳转到 searchResult.jsp 页面*/
        return "fore/searchResult";
    }

    @RequestMapping("forebuyone")
    /*获取参数pid,获取参数num*/
    public String buyone(int pid, int num, HttpSession session) {
        /*根据pid获取产品对象p*/
        Product p = productService.get(pid);
        int oiid = 0;

        /*从session中获取用户对象user*/
        User user =(User)  session.getAttribute("user");
        /*如果已经存在这个产品对应的OrderItem，并且还没有生成订单，即还在购物车中。 那么就应该在对应的OrderItem基础上，调整数量*/
        boolean found = false;
        /*基于用户对象user，查询没有生成订单的订单项集合*/
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        /*遍历这个集合*/
        for (OrderItem oi : ois) {
             /*如果产品是一样的话，就进行数量追加*/
            if(oi.getProduct().getId().intValue()==p.getId().intValue()){
                oi.setNumber(oi.getNumber()+num);
                orderItemService.update(oi);
                found = true;
                /*获取这个订单项的 id*/
                oiid = oi.getId();
                break;
            }
        }

        /*如果不存在对应的OrderItem,那么就新增一个订单项OrderItem*/
        if(!found){
            /*生成新的订单项*/
            OrderItem oi = new OrderItem();
            /*设置数量，用户和产品*/
            oi.setUid(user.getId());
            oi.setNumber(num);
            oi.setPid(pid);
            /*插入到数据库*/
            orderItemService.add(oi);
            /*获取这个订单项的 id*/
            oiid = oi.getId();
        }
        /*基于这个订单项id客户端跳转到结算页面/forebuy*/
        return "redirect:forebuy?oiid="+oiid;
    }

    @RequestMapping("forebuy")
    /*通过字符串数组获取参数oiid*/
    /*为什么这里要用字符串数组试图获取多个oiid，而不是int类型仅仅获取一个oiid? 因为根据购物流程环节与表关系，结算页面还需要显示在购物车中选中的多条OrderItem数据，所以为了兼容从购物车页面跳转过来的需求，要用字符串数组获取多个oiid*/
    public String buy( Model model,String[] oiid,HttpSession session){
        /*准备一个泛型是OrderItem的集合ois*/
        List<OrderItem> ois = new ArrayList<>();
        float total = 0;

        for (String strid : oiid) {
            int id = Integer.parseInt(strid);
            /*根据前面步骤获取的oiids，从数据库中取出OrderItem对象，并放入ois集合中*/
            OrderItem oi= orderItemService.get(id);
            /*累计这些ois的价格总数，赋值在total上*/
            total +=oi.getProduct().getPromotePrice()*oi.getNumber();
            ois.add(oi);
        }

        /*把订单项集合放在session的属性 "ois" 上*/
        session.setAttribute("ois", ois);
        /*把总价格放在 model的属性 "total" 上*/
        model.addAttribute("total", total);
        /*服务端跳转到buy.jsp*/
        return "fore/buy";
    }

    @RequestMapping("foreaddCart")
    @ResponseBody
    /*获取参数pid,获取参数num*/
    public String addCart(int pid, int num, Model model,HttpSession session) {
        /*根据pid获取产品对象p*/
        Product p = productService.get(pid);
        /*从session中获取用户对象user*/
        User user =(User)  session.getAttribute("user");
        /*如果已经存在这个产品对应的OrderItem，并且还没有生成订单，即还在购物车中。 那么就应该在对应的OrderItem基础上，调整数量*/
        boolean found = false;
        /*基于用户对象user，查询没有生成订单的订单项集合*/
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        /*遍历这个集合*/
        for (OrderItem oi : ois) {
            if(oi.getProduct().getId().intValue()==p.getId().intValue()){
                /*如果产品是一样的话，就进行数量追加*/
                oi.setNumber(oi.getNumber()+num);
                /*获取这个订单项的 id*/
                orderItemService.update(oi);
                found = true;
                break;
            }
        }

        /*如果不存在对应的OrderItem,那么就新增一个订单项OrderItem*/
        if(!found){
            /*生成新的订单项*/
            OrderItem oi = new OrderItem();
            /*获取这个订单项的 id*/
            oi.setUid(user.getId());
            /*设置数量，用户和产品*/
            oi.setNumber(num);
            oi.setPid(pid);
            /*插入到数据库*/
            orderItemService.add(oi);
        }
        /* 最后返回字符串"success"*/
        return "success";
    }

    @RequestMapping("forecart")
    public String cart( Model model,HttpSession session) {
        /*通过session获取当前用户*/
        /*所以一定要登录才访问，否则拿不到用户对象,会报错*/
        User user =(User)  session.getAttribute("user");
        /*获取为这个用户关联的订单项集合 ois*/
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        /*把ois放在model中*/
        model.addAttribute("ois", ois);
        /*服务端跳转到cart.jsp*/
        return "fore/cart";
    }

    @RequestMapping("forechangeOrderItem")
    @ResponseBody
    /*获取pid和number*/
    public String changeOrderItem( Model model,HttpSession session, int pid, int number) {
        User user =(User)  session.getAttribute("user");
        /*判断用户是否登录*/
        if(null==user)
            return "fail";

        /*遍历出用户当前所有的未生成订单的OrderItem*/
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        for (OrderItem oi : ois) {
            if(oi.getProduct().getId().intValue()==pid){
                oi.setNumber(number);
                /*根据pid找到匹配的OrderItem，并修改数量后更新到数据库*/
                orderItemService.update(oi);
                break;
            }
        }
        /*返回字符串"success"*/
        return "success";
    }

    @RequestMapping("foredeleteOrderItem")
    @ResponseBody
    /*获取oiid*/
    public String deleteOrderItem( Model model,HttpSession session,int oiid){
        User user =(User)  session.getAttribute("user");
        /*判断用户是否登录*/
        if(null==user)
            return "fail";
        /*删除oiid对应的OrderItem数据*/
        orderItemService.delete(oiid);
        /*返回字符串"success"*/
        return "success";
    }

    @RequestMapping("forecreateOrder")
    public String createOrder( Model model,Order order,HttpSession session){
        /*从session中获取user对象*/
        User user =(User)  session.getAttribute("user");
        /*根据当前时间加上一个4位随机数生成订单号*/
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        /*通过参数Order接受地址，邮编，收货人，用户留言等信息*/
        order.setOrderCode(orderCode);
        /*根据上述参数，创建订单对象*/
        order.setCreateDate(new Date());
        order.setUid(user.getId());
        /*把订单状态设置为等待支付*/
        order.setStatus(OrderService.waitPay);
        /*从session中获取订单项集合*/
        /* 在结算功能的ForeController.buy() 278行，订单项集合被放到了session中*/
        List<OrderItem> ois= (List<OrderItem>)  session.getAttribute("ois");

        /*把订单加入到数据库，并且遍历订单项集合，设置每个订单项的order，更新到数据库*/
        /*统计本次订单的总金额*/
        float total =orderService.add(order,ois);
        /*客户端跳转到确认支付页forealipay，并带上订单id和总金额*/
        return "redirect:forealipay?oid="+order.getId() +"&total="+total;
    }

    @RequestMapping("forepayed")
    /*获取参数oid*/
    public String payed(int oid, float total, Model model) {
        /*根据oid获取到订单对象order*/
        Order order = orderService.get(oid);
        /*修改订单对象的状态和支付时间*/
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        /*更新这个订单对象到数据库*/
        orderService.update(order);
        /*把这个订单对象放在model的属性"o"上*/
        model.addAttribute("o", order);
        /*服务端跳转到payed.jsp*/
        return "fore/payed";
    }

    @RequestMapping("forebought")
    public String bought( Model model,HttpSession session) {
        /*通过session获取用户user*/
        User user =(User)  session.getAttribute("user");
        /*查询user所有的状态不是"delete" 的订单集合os*/
        List<Order> os= orderService.list(user.getId(),OrderService.delete);

        /*为这些订单填充订单项*/
        orderItemService.fill(os);

        /*把os放在model的属性"os"上*/
        model.addAttribute("os", os);

        /*服务端跳转到bought.jsp*/
        return "fore/bought";
    }

    @RequestMapping("foreconfirmPay")
    /*获取参数oid*/
    public String confirmPay( Model model,int oid) {
        /*通过oid获取订单对象o*/
        Order o = orderService.get(oid);
        /*为订单对象填充订单项*/
        orderItemService.fill(o);
        /*把订单对象放在request的属性"o"上*/
        model.addAttribute("o", o);
        /*服务端跳转到 confirmPay.jsp*/
        return "fore/confirmPay";
    }

    @RequestMapping("foreorderConfirmed")
    /*获取参数oid*/
    public String orderConfirmed( Model model,int oid) {
        /*根据参数oid获取Order对象o*/
        Order o = orderService.get(oid);
        /*修改对象o的状态为等待评价，修改其确认支付时间*/
        o.setStatus(OrderService.waitReview);
        o.setConfirmDate(new Date());
        /*更新到数据库*/
        orderService.update(o);
        /*服务端跳转到orderConfirmed.jsp页面*/
        return "fore/orderConfirmed";
    }

    @RequestMapping("foredeleteOrder")
    @ResponseBody
    /*获取参数oid*/
    public String deleteOrder( Model model,int oid){
        /*根据oid获取订单对象o*/
        Order o = orderService.get(oid);
        /*修改状态*/
        o.setStatus(OrderService.delete);
        /*更新到数据库*/
        orderService.update(o);
        /*返回字符串"success"*/
        return "success";
    }

    @RequestMapping("forereview")
    /*获取参数oid*/
    public String review( Model model,int oid) {
        /*根据oid获取订单对象o*/
        Order o = orderService.get(oid);
        /*为订单对象填充订单项*/
        orderItemService.fill(o);
        /*获取第一个订单项对应的产品,因为在评价页面需要显示一个产品图片，那么就使用这第一个产品的图片了*/
        Product p = o.getOrderItems().get(0).getProduct();
        /*获取这个产品的评价集合*/
        List<Review> reviews = reviewService.list(p.getId());
        /*为产品设置评价数量和销量*/
        productService.setSaleAndReviewNumber(p);
        /*把产品，订单和评价集合放在request上*/
        model.addAttribute("p", p);
        model.addAttribute("o", o);
        model.addAttribute("reviews", reviews);
        /*服务端跳转到 review.jsp*/
        return "fore/review";
    }


    @RequestMapping("foredoreview")
    /*获取参数oid*/
    public String doreview( Model model,HttpSession session,@RequestParam("oid") int oid,@RequestParam("pid") int pid,String content) {
        /*根据oid获取订单对象o*/
        Order o = orderService.get(oid);
        /*修改订单对象状态*/
        o.setStatus(OrderService.finish);
        /*更新订单对象到数据库*/
        orderService.update(o);

        /*获取参数pid*/
        /*根据pid获取产品对象*/
        Product p = productService.get(pid);
        /*获取参数content (评价信息)*/
        /*对评价信息进行转义，道理同注册ForeController.register()*/
        content = HtmlUtils.htmlEscape(content);

        /*从session中获取当前用户*/
        User user =(User)  session.getAttribute("user");
        /*创建评价对象review*/
        Review review = new Review();
        /*为评价对象review设置 评价信息，产品，时间，用户*/
        review.setContent(content);
        review.setPid(pid);
        review.setCreateDate(new Date());
        review.setUid(user.getId());
        /*增加到数据库*/
        reviewService.add(review);

        /*客户端跳转到/forereview： 评价产品页面，并带上参数showonly=true*/
        return "redirect:forereview?oid="+oid+"&showonly=true";
    }

}
