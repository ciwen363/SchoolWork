package com.hmdp;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.utils.RedisConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.connection.RedisConnection;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@SpringBootTest
public class SyncUserToRedisTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void syncUsersToRedis() {
        // 1. 从数据库查询前1000个用户
        List<User> users = userMapper.selectList(null);

        if (users == null || users.isEmpty()) {
            System.out.println("数据库中没有用户数据");
            return;
        }

        // 限制最多1000个用户
        List<User> targetUsers = users.stream()
                .limit(1000)
                .collect(Collectors.toList());

        System.out.println("开始同步用户数据到Redis，共 " + targetUsers.size() + " 个用户");

        int successCount = 0;
        int failCount = 0;

        // 2. 遍历用户列表，将每个用户信息存入Redis
        for (User user : targetUsers) {
            try {
                // 创建UserDTO对象（只包含必要字段）
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setNickName(user.getNickName());
                userDTO.setIcon(user.getIcon());

                // 生成token（使用UUID）
                String token = UUID.randomUUID().toString().replace("-", "");

                // 构造Redis key
                String key = RedisConstants.LOGIN_USER_KEY + token;

                // 将UserDTO转换为Map并存入Redis Hash结构
                Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(), CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));

                stringRedisTemplate.opsForHash().putAll(key, userMap);

                // 设置过期时间（30分钟）
                stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

                successCount++;

                if (successCount % 100 == 0) {
                    System.out.println("已同步 " + successCount + " 个用户");
                }
            } catch (Exception e) {
                failCount++;
                System.err.println("同步用户失败，用户ID: " + user.getId() + ", 错误: " + e.getMessage());
            }
        }

        System.out.println("========================================");
        System.out.println("同步完成！");
        System.out.println("成功: " + successCount + " 个");
        System.out.println("失败: " + failCount + " 个");
        System.out.println("========================================");
    }


    @Test
    public void exportTokensToFile() {
        // 指定输出文件路径（修改为你想要的目录）
        String outputPath = "D:/Workspace/dianping/tokens.txt";

        System.out.println("开始从Redis导出token到文件: " + outputPath);

        int count = 0;
        int errorCount = 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            // 使用SCAN命令遍历所有login:token:*的key
            String pattern = RedisConstants.LOGIN_USER_KEY + "*";

            System.out.println("搜索模式: " + pattern);

            ScanOptions scanOptions = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)
                    .build();

            Cursor<byte[]> cursor = stringRedisTemplate.execute((RedisConnection connection) ->
                    connection.scan(scanOptions)
            );

            while (cursor.hasNext()) {
                try {
                    byte[] keyBytes = cursor.next();
                    String key = new String(keyBytes);

                    // 调试：打印前5个key看看实际格式
                    if (count < 5) {
                        System.out.println("找到的Key: " + key);
                    }

                    // 从key中提取token部分（去掉 "login:token:" 前缀）
                    if (key.startsWith(RedisConstants.LOGIN_USER_KEY)) {
                        String token = key.substring(RedisConstants.LOGIN_USER_KEY.length());

                        // 写入文件，每个token一行
                        writer.write(token);
                        writer.newLine();

                        count++;

                        if (count % 100 == 0) {
                            System.out.println("已导出 " + count + " 个token");
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("处理token失败: " + e.getMessage());
                }
            }

            cursor.close();

            System.out.println("========================================");
            System.out.println("导出完成！");
            System.out.println("成功导出: " + count + " 个token");
            System.out.println("失败: " + errorCount + " 个");
            System.out.println("文件路径: " + outputPath);
            System.out.println("========================================");

        } catch (IOException e) {
            System.err.println("文件写入失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void debugRedisKeys() {
        System.out.println("=== 调试Redis中的Key ===");

        // 查找所有包含 login:token 的key
        String pattern = "*login:token*";
        System.out.println("搜索模式: " + pattern);

        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(pattern)
                .count(10)
                .build();

        Cursor<byte[]> cursor = stringRedisTemplate.execute((RedisConnection connection) ->
                connection.scan(scanOptions)
        );

        int count = 0;
        while (cursor.hasNext() && count < 10) {
            byte[] keyBytes = cursor.next();
            String key = new String(keyBytes);
            System.out.println("Key: " + key);

            // 检查key的类型
            String type = String.valueOf(stringRedisTemplate.type(key));
            System.out.println("  类型: " + type);

            count++;
        }

        cursor.close();
        System.out.println("========================");
    }
}
