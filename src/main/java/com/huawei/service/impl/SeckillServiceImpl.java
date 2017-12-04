package com.huawei.service.impl;

import com.huawei.dao.SeckillDao;
import com.huawei.dao.SuccessKilledDao;
import com.huawei.dto.Exposer;
import com.huawei.dto.SeckillExecution;
import com.huawei.entity.Seckill;
import com.huawei.entity.SuccessKilled;
import com.huawei.enums.SeckillStatEnum;
import com.huawei.exception.RepeatKillException;
import com.huawei.exception.SeckillCloseException;
import com.huawei.exception.SeckillException;
import com.huawei.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private SuccessKilledDao successKilledDao;
    private SeckillDao seckillDao;

    //盐值，混淆
    private final String slat = "asdasdfHIUHiisaudfa;asd&8787y8iuq3wfhbaksjdfha9q3e$%&*^&8aksdfa";

    public List<Seckill> getSeckillList() {

        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {

        Seckill seckill = seckillDao.queryById(seckillId);
        if (seckill == null) {
            return new Exposer(false, seckillId);
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        //转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    public SeckillExecution executeSeckill(long seckillId, long userPhonne, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || md5.equals(getMD5(seckillId))) {
            throw new SeckillException("seckill data rewrite");
        }
        //执行秒杀：减库存 + 记录购买行为
        Date nowTime = new Date();

        try {
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if (updateCount <= 0) {
                throw new SeckillCloseException("seckill is close");
            } else {
                // 记录购买行为
                int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhonne);
                if (insertCount <= 0) {
                    throw new RepeatKillException("seckill repeated");
                } else {
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhonne);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e) {
            throw e;
        } catch (RepeatKillException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // 所有编译期异常转化为运行期异常
            throw new SeckillException("seckill inner error" + e.getMessage());
        }
    }
}
