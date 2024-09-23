package com.packshop.client.viewmodel.home;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeBannerModel {
    private String title;
    private String imageUrl;
    private String link;
}
