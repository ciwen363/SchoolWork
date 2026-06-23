package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.limiter.annotation.RateLimiter;
import com.hmdp.service.IVoucherOrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ciwen
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @PostMapping("seckill/{id}")
//    @GetMapping("seckill/{id}")  // 注意为了测试限流效果,这里直接改成了Get请求方式,直接用网页刷新就请求了  http://127.0.0.1:8081/voucher-order/seckill/1
//    @RateLimiter(
//            key = "coupon:seckill:",
//            window = 10,
//            limit = 5,
//            message = "秒杀活动太火爆，请稍后再试",
//            type = RateLimiter.LimitType.METHOD
//    )
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
         return voucherOrderService.seckillVoucher(voucherId);
//        return Result.ok();   // 为了测试限流效果,就不执行内部逻辑了,如果没有被限流就直接返回成功
    }
}
