package com.example.kakaotest;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.common.util.KakaoCustomTabsClient;
import com.kakao.sdk.common.util.Utility;
import com.kakao.sdk.share.ShareClient;
import com.kakao.sdk.share.WebSharerClient;
import com.kakao.sdk.template.model.*;
import com.kakao.sdk.user.UserApiClient;
import com.unity3d.player.UnityPlayer;

import java.util.Arrays;

public class MyPlugin {
    private static MyPlugin _instance;
    private static Activity _context;

    public static MyPlugin instance() {
        if(_instance == null) {
            _instance = new MyPlugin();
            _context = UnityPlayer.currentActivity;
        }
        return _instance;
    }

    public void KakaoShare(String title, String description, String btnText, String imgUrl, String webUrl, String mobileWebUrl) {

        String TAG = "KakaoShare";

        // com.samsungfire.sandboxdevomoomo
        KakaoSdk.init(_context, "21a9d6f4f57bcfbb74a463d0d6b5f02f");

        // com.chad.KakaoPlugin - 샘플로 전달한 테스트 프로젝트용
        // KakaoSdk.init(_context, "14fd472ef21d4d612cec76669f141eff");

        // 키 해시 체크
        // String keyHash = Utility.INSTANCE.getKeyHash(_context);
        // Log.e(TAG, keyHash);

        if (ShareClient.getInstance().isKakaoTalkSharingAvailable(_context)) {
            FeedTemplate feedTemplate = new FeedTemplate(
                    new Content(title, imgUrl, new Link(webUrl, mobileWebUrl), description),
                    null,
                    null,
                    Arrays.asList(new com.kakao.sdk.template.model.Button(btnText, new Link(webUrl, mobileWebUrl)))
            );

            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(_context)) {
                kakaoLink(feedTemplate);
            } else {
                webKakaoLink(feedTemplate);
            }
        }
    }

    public void kakaoLink(FeedTemplate feedTemplate) {
        String TAG = "kakaoLink";
        // 카카오톡으로 카카오링크 공유 가능
        ShareClient.getInstance().shareDefault(_context, feedTemplate, null, (linkResult, error) -> {
            if (error != null) {
                Log.e(TAG, "카카오링크 보내기 실패", error);
            } else if (linkResult != null) {
                Log.d(TAG, "카카오링크 보내기 성공 ${linkResult.intent}");
                _context.startActivity(linkResult.getIntent());

                // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                Log.w(TAG, "Warning Msg: " + linkResult.getWarningMsg());
                Log.w(TAG, "Argument Msg: " + linkResult.getArgumentMsg());
            }
            return null;
        });
    }

    public void webKakaoLink(FeedTemplate feedTemplate) {
        String TAG = "webKakaoLink";

        // 카카오톡 미설치: 웹 공유 사용 권장
        // 웹 공유 예시 코드
        //Uri sharerUrl = WebSharerClient.getInstance().defaultTemplateUri(feedTemplate);
        Uri sharerUrl = WebSharerClient.getInstance().makeDefaultUrl(feedTemplate);


        // CustomTabs으로 웹 브라우저 열기
        // 1. CustomTabs으로 Chrome 브라우저 열기
        try {
            KakaoCustomTabsClient.INSTANCE.openWithDefault(_context, sharerUrl);
        } catch (UnsupportedOperationException e) {
            // Chrome 브라우저가 없을 때 예외처리
        }

        // 2. CustomTabs으로 디바이스 기본 브라우저 열기
        try {
            KakaoCustomTabsClient.INSTANCE.open(_context, sharerUrl);
        } catch (ActivityNotFoundException e) {
            // 인터넷 브라우저가 없을 때 예외처리
        }
    }
}
