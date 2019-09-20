/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2014-2019. All rights reserved.
 */

package com.example.hmsdemo;

public interface OaidCallback {
    void onSuccuss(String oaid, boolean isOaidTrackLimited);

    void onFail(String errMsg);
}
