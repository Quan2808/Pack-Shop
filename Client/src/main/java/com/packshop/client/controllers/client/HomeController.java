package com.packshop.client.controllers.client;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.packshop.client.viewmodel.home.HomeBannerModel;
import com.packshop.client.viewmodel.home.HomeServiceModel;
import com.packshop.client.viewmodel.home.HomeSlideModel;

@Controller
public class HomeController {

    private static final String LAYOUT = "client/fragments/_layout";

    public static String renderView(Model model, String templateName, String title) {
        model.addAttribute("view", templateName);
        model.addAttribute("title", title);
        return LAYOUT;
    }

    @GetMapping()
    public String home(Model model) {
        List<HomeServiceModel> services = List.of(
                new HomeServiceModel("Công ty may balo túi xách cao cấp thời trang uy tín chất lượng hàng đầu",
                        "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_1.jpg",
                        "service1"),
                new HomeServiceModel("Thiết kế, sản xuất balo, túi xách theo yêu cầu số lượng lớn cho các doanh nghiệp",
                        "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_2.jpg",
                        "service2"),
                new HomeServiceModel("Hệ Thống Đại Lý Phân Phối Thương Hiệu Pack Shop",
                        "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_3.jpg",
                        "service3")
        );

        List<HomeSlideModel> slides = List.of(
                new HomeSlideModel("https://theme.hstatic.net/200000273565/1000933517/14/slideshow_4.jpg",
                        "Motorbike Smoke"),
                new HomeSlideModel("https://theme.hstatic.net/200000273565/1000933517/14/slideshow_3.jpg",
                        "Mountaintop"),
                new HomeSlideModel("https://theme.hstatic.net/200000273565/1000933517/14/slideshow_2.jpg",
                        "Woman Reading a Book")
        );

        List<HomeBannerModel> banners = List.of(
                new HomeBannerModel("Tại sao chọn Kingbag?",
                        "https://theme.hstatic.net/200000273565/1000933517/14/home_category_1_banner.jpg",
                        "banner1"),
                new HomeBannerModel("Quy trình đặt hàng",
                        "https://theme.hstatic.net/200000273565/1000933517/14/home_category_2_banner.jpg",
                        "banner2"),
                new HomeBannerModel("Câu hỏi thường gặp",
                        "https://theme.hstatic.net/200000273565/1000933517/14/home_category_3_banner.jpg",
                        "banner3")
        );      

        model.addAttribute("slides", slides);
        model.addAttribute("services", services);
        model.addAttribute("banners", banners);
        return renderView(model, "client/pages/home/index", "Home");
    }

    @GetMapping("/introduce")
    public String introduce(Model model) {
        return renderView(model, "client/pages/introduce/index", "Introduce");
    }
}
