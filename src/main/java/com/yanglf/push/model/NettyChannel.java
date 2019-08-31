package com.yanglf.push.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author yanglf
 * @description
 * @since 2019/8/31
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NettyChannel {
    private String id;
    private String name;
    private Long userId;
    private Date createTime;
}
