package org.almagestauth.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.almagestauth.domain.entity.MobileAppVersion;
import org.almagestauth.domain.entity.QMobileAppVersion;
import org.almagestauth.exception.r400.IllegalArgumentException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppVersionService {
    private final JPAQueryFactory query;


    public MobileAppVersion getAppversion(){
        QMobileAppVersion qMobileAppVersion = QMobileAppVersion.mobileAppVersion;

        MobileAppVersion appVersion = query.selectFrom(qMobileAppVersion)
                .where(qMobileAppVersion.isCurrent.eq("T"))
                .fetchOne();
        if(appVersion == null){
            throw new IllegalArgumentException("앱 버전 조회 실패");
        }
        return appVersion;
    }

    public void setNewVersion(){

    }


}
