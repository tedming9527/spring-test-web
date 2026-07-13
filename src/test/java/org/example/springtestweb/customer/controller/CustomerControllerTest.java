package org.example.springtestweb.customer.controller;

import org.example.springtestweb.customer.entity.Customer;
import org.example.springtestweb.customer.mapper.CustomerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class CustomerControllerTest {

    private CustomerMapper customerMapper;
    private CustomerController customerController;

    /**
     * 每个测试方法执行前自动调用，初始化测试环境
     */
    @BeforeEach
    void setUp() {
        // 创建 CustomerMapper 的 Mock 对象，不启动 Spring 容器，不连接数据库
        customerMapper = mock(CustomerMapper.class);

        // 手动实例化 Controller（而非 Spring 注入）
        customerController = new CustomerController();

        // 通过反射将 mock 的 mapper 注入到 controller 的 @Autowired 字段中
        // 因为不启动 Spring，无法使用 @Autowired 自动注入
        try {
            var mapperField = CustomerController.class.getDeclaredField("customerMapper");
            mapperField.setAccessible(true); // 绕过 private 访问限制
            mapperField.set(customerController, customerMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构造测试用 Customer 对象的辅助方法
     */
    private Customer mockCustomer(Long id, String name, Integer age, String email) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setAge(age);
        customer.setEmail(email);
        return customer;
    }

    /**
     * 测试：传入有效 ID，返回对应的 Customer 对象
     *
     * 流程：
     *   1. 配置 mock mapper：当被调用 selectById(1L) 时，返回我们准备好的 expected 对象
     *   2. 调用 controller.getCustomer(1L)
     *   3. controller 内部调用 mapper.selectById(1L)，触发 mock 规则，拿到 expected
     *   4. controller 把 expected 返回给测试
     *   5. 验证返回结果正确
     */
    @Test
    void getCustomer_validId_returnsCustomer() {
        // Arrange: 准备"假数据"——这是 mock mapper 要返回的内容，不是注入 controller 的
        Customer expected = mockCustomer(1L, "Jone", 18, "test1@baomidou.com");

        // 关键：配置 mock 行为规则
        // "当 customerMapper.selectById(1L) 被调用时，返回 expected 对象"
        // 此时 controller 还没调用 mapper，只是提前设定好应答规则
        when(customerMapper.selectById(1L)).thenReturn(expected);

        // Act: 调用 controller 方法
        // controller 内部会执行 customerMapper.selectById(1L)
        // 因为 mapper 是 mock，所以不会查数据库，而是按上面的规则返回 expected
        Customer result = customerController.getCustomer(1L);

        // Assert: result 就是 expected（controller 透传了 mapper 的返回值）
        assertNotNull(result);
        assertEquals("Jone", result.getName());
        assertEquals(18, result.getAge());
        assertEquals("test1@baomidou.com", result.getEmail());

        // 验证 mapper.selectById(1L) 确实被 controller 调用了一次
        verify(customerMapper).selectById(1L);
    }

    /**
     * 测试：传入不存在的 ID，返回 null
     *
     * 流程：
     *   1. 配置 mock mapper：当被调用 selectById(999L) 时，返回 null（模拟数据库无此记录）
     *   2. 调用 controller.getCustomer(999L)
     *   3. controller 内部调用 mapper.selectById(999L)，mock 返回 null
     *   4. controller 透传 null 给调用者
     *   5. 验证返回结果为 null
     */
    @Test
    void getCustomer_invalidId_returnsNull() {
        // Arrange: 配置 mock 规则——selectById(999L) 返回 null，模拟"查不到记录"
        when(customerMapper.selectById(999L)).thenReturn(null);

        // Act: 调用 controller，controller 内部调用 mapper，mapper 按规则返回 null
        Customer result = customerController.getCustomer(999L);

        // Assert: controller 透传了 mapper 的返回值，所以 result 也是 null
        assertNull(result);

        // 验证 mapper.selectById(999L) 确实被 controller 调用了一次
        verify(customerMapper).selectById(999L);
    }
}
