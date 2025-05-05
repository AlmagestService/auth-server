package org.almagestauth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class FCMNotificationRequestDto {
    private String targetMemberId;
    private String title;
    private String body;
//    private String image;
//    private Map<String, String> data;


    @Builder
    public FCMNotificationRequestDto(String targetMemberId, String title, String body){
        this.targetMemberId = targetMemberId;
        this.title = title;
        this.body = body;
//        this.image = image;
//        this.data = data;
    }
}
