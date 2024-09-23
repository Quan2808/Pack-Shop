package com.packshop.client.controllers.client;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
        List<HomeServiceModel> allServices = List.of(
                new HomeServiceModel("Công ty may balo túi xách cao cấp thời trang uy tín chất lượng hàng đầu",
                        "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_1.jpg?v=602",
                        "service1"),
                new HomeServiceModel("Thiết kế, sản xuất balo, túi xách theo yêu cầu số lượng lớn cho các doanh nghiệp",
                        "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_2.jpg?v=602",
                        "service2"),
                new HomeServiceModel("Hệ Thống Đại Lý Phân Phối Thương Hiệu Pack Shop",
                        "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_3.jpg?v=602",
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

        model.addAttribute("slides", slides);
        model.addAttribute("topServices", allServices);
        return renderView(model, "client/pages/home", "Home");
    }

    @GetMapping("/introduce")
    public String introduce(Model model) {
        return renderView(model, "client/home", "Home");
    }
}
