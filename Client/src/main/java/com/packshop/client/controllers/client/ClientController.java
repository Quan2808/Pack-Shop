package com.packshop.client.controllers.client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientController {

    private static final String LAYOUT = "client/fragments/_layout";

    public static String renderView(Model model, String templateName, String title) {
        model.addAttribute("view", templateName);
        model.addAttribute("title", title);
        return LAYOUT;
    }

    @GetMapping()
    public String home(Model model) {
        List<Map<String, String>> services = List.of(
                Map.of(
                        "title", "Công ty may balo túi xách cao cấp thời trang uy tín chất lượng hàng đầu",
                        "image", "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_1.jpg",
                        "link", "service1"
                ),
                Map.of(
                        "title", "Thiết kế, sản xuất balo, túi xách theo yêu cầu số lượng lớn cho các doanh nghiệp",
                        "image", "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_2.jpg",
                        "link", "service2"
                ),
                Map.of(
                        "title", "Hệ Thống Đại Lý Phân Phối Thương Hiệu Pack Shop",
                        "image", "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_3.jpg",
                        "link", "service3"
                )
        );

        List<Map<String, String>> slides = List.of(
                Map.of("image", "https://theme.hstatic.net/200000273565/1000933517/14/slideshow_4.jpg",
                        "caption", "Motorbike Smoke"),
                Map.of("image", "https://theme.hstatic.net/200000273565/1000933517/14/slideshow_3.jpg",
                        "caption", "Mountaintop"),
                Map.of("image", "https://theme.hstatic.net/200000273565/1000933517/14/slideshow_2.jpg",
                        "caption", "Woman Reading a Book")
        );

        List<Map<String, String>> banners = List.of(
                Map.of(
                        "title", "Tại sao chọn Kingbag?",
                        "image", "https://theme.hstatic.net/200000273565/1000933517/14/home_category_1_banner.jpg",
                        "link", "introduce"
                ),
                Map.of(
                        "title", "Quy trình đặt hàng",
                        "image", "https://theme.hstatic.net/200000273565/1000933517/14/home_category_2_banner.jpg",
                        "link", "banner2"
                ),
                Map.of(
                        "title", "Câu hỏi thường gặp",
                        "image", "https://theme.hstatic.net/200000273565/1000933517/14/home_category_3_banner.jpg",
                        "link", "banner3"
                )
        );

        model.addAttribute("slides", slides);
        model.addAttribute("services", services);
        model.addAttribute("banners", banners);
        return renderView(model, "client/pages/home/index", "Home");
    }

    @GetMapping("/introduce")
    public String introduce(Model model) {
        List<Map<String, String>> reasons = Arrays.asList(
                Map.of("icon", "fa-solid fa-shield-halved",
                        "title", "Responsibility",
                        "description", "Employees are accountable, and the company considers its impact."),
                Map.of("icon", "fa-regular fa-lightbulb",
                        "title", "Open-minded",
                        "description", "Values diverse ideas and fosters creativity."),
                Map.of("icon", "fa-solid fa-chart-line",
                        "title", "Efficiency",
                        "description", "Optimizes resources for swift and effective goal achievement."),
                Map.of("icon", "fa-regular fa-thumbs-up",
                        "title", "Recognition",
                        "description", "Appreciates and recognizes individual contributions and efforts.")
        );
        model.addAttribute("reasons", reasons);
        return renderView(model, "client/pages/introduce/index", "Introduce");
    }
}
