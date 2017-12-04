package com.huawei.service;

import com.huawei.dto.Exposer;
import com.huawei.dto.SeckillExecution;
import com.huawei.entity.Seckill;
import com.huawei.exception.RepeatKillException;
import com.huawei.exception.SeckillCloseException;
import com.huawei.exception.SeckillException;

import java.util.List;

public interface SeckillService {

    /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     *  查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启输出接口地址
     * 否则输出系统时间和秒杀时间
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀
     * @param seckillId
     * @param userPhonne
     * @param md5
     */
    SeckillExecution executeSeckill(long seckillId, long userPhonne, String md5)
    throws SeckillException, RepeatKillException, SeckillCloseException;
}
