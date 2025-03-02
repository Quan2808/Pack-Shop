package com.packshop.client.modules.client.home.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.packshop.client.common.utilities.ViewRenderer;

@Controller
public class HomeController {

    private final ViewRenderer viewRenderer;

    HomeController(ViewRenderer viewRenderer) {
        this.viewRenderer = viewRenderer;
    }

    @GetMapping()
    public String home(Model model) {

        List<Map<String, String>> services = List.of(Map.of("title",
                "The Leading Company in Quality and Trusted Backpack and Handbag Manufacturing",
                "image",
                "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_1.jpg", "link",
                "/services/leading-bag-manufacturing"),
                Map.of("title", "Custom manufacturing - high-end backpacks and handbags nationwide",
                        "image",
                        "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_2.jpg",
                        "link", "/services/custom-manufacturing"),
                Map.of("title",
                        "The Leading Company in Quality and Trusted Backpack and Handbag Manufacturing",
                        "image",
                        "https://theme.hstatic.net/200000273565/1000933517/14/h-service_item_3.jpg",
                        "link", "/services/brand-distribution"));

        List<Map<String, String>> slides = List.of(
                Map.of("image",
                        "https://theme.hstatic.net/200000273565/1000933517/14/slideshow_4.jpg",
                        "caption", "Motorbike Smoke"),
                Map.of("image",
                        "https://theme.hstatic.net/200000273565/1000933517/14/slideshow_3.jpg",
                        "caption", "Mountaintop"),
                Map.of("image",
                        "https://theme.hstatic.net/200000273565/1000933517/14/slideshow_2.jpg",
                        "caption", "Woman Reading a Book"));

        List<Map<String, String>> banners = List.of(Map.of("title", "Tại sao chọn Kingbag?",
                "image",
                "https://theme.hstatic.net/200000273565/1000933517/14/home_category_1_banner.jpg",
                "link", "introduce"),
                Map.of("title", "Order process", "image",
                        "https://theme.hstatic.net/200000273565/1000933517/14/home_category_2_banner.jpg",
                        "link", "/services/order-process"),
                Map.of("title", "Câu hỏi thường gặp", "image",
                        "https://theme.hstatic.net/200000273565/1000933517/14/home_category_3_banner.jpg",
                        "link", "/services/faq"));

        model.addAttribute("slides", slides);
        model.addAttribute("services", services);
        model.addAttribute("banners", banners);
        return viewRenderer.renderView(model, "client/home/index", "Home");
    }

    // Introduce and Contact Pages
    @GetMapping("/{page}")
    public String renderStaticPage(@PathVariable("page") String page, Model model) {
        String templateName = "client/static/" + page + "/index";
        String title;

        switch (page) {
            case "introduce" -> {
                title = "Introduce";
                List<Map<String, String>> reasons = Arrays.asList(
                        Map.of("icon", "fa-solid fa-shield-halved", "title", "Responsibility",
                                "description",
                                "Employees are accountable, and the company considers its impact."),
                        Map.of("icon", "fa-regular fa-lightbulb", "title", "Open-minded",
                                "description", "Values diverse ideas and fosters creativity."),
                        Map.of("icon", "fa-solid fa-chart-line", "title", "Efficiency",
                                "description",
                                "Optimizes resources for swift and effective goal achievement."),
                        Map.of("icon", "fa-regular fa-thumbs-up", "title", "Recognition",
                                "description",
                                "Appreciates and recognizes individual contributions and efforts."));
                model.addAttribute("reasons", reasons);
            }
            case "contact" -> title = "Contact";
            default -> throw new IllegalArgumentException("Invalid page name: " + page);
        }

        return viewRenderer.renderView(model, templateName, title);
    }

    // Service Pages
    @GetMapping("/services/{serviceName}")
    public String renderServicePage(@PathVariable("serviceName") String serviceName, Model model) {
        String templateName = "client/static/services/" + serviceName;
        String title;

        switch (serviceName) {
            case "leading-bag-manufacturing" -> title = "Leading Bag Manufacturing";
            case "custom-manufacturing" -> title = "Custom Manufacturing";
            case "brand-distribution" -> title = "Brand Distribution";
            case "order-process" -> title = "Order Process";
            case "faq" -> title = "Frequently Asked Questions";
            default -> throw new IllegalArgumentException("Invalid service name: " + serviceName);
        }

        return viewRenderer.renderView(model, templateName, title);
    }

    // Legal Pages
    @GetMapping("/legal/{legalName}")
    public String renderLegalPage(@PathVariable("legalName") String legalName, Model model) {
        String templateName = "client/static/legal/" + legalName;
        String title;

        switch (legalName) {
            case "return-exchange-policy" -> title = "Return and Exchange Policy";
            case "privacy-policy" -> title = "Privacy Policy";
            case "term-of-service" -> title = "Term of Service";
            default -> throw new IllegalArgumentException("Invalid legal name: " + legalName);
        }

        return viewRenderer.renderView(model, templateName, title);
    }
}
