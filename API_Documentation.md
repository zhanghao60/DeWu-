# Django CV 项目 API 接口文档

## 项目概述

这是一个基于Django的计算机视觉项目，提供图像模板匹配功能。项目使用OpenCV进行图像处理，能够在上传的目标图像中查找指定的模板图像，并返回匹配位置的坐标信息。

## 技术栈

- **后端框架**: Django 5.2.7
- **图像处理**: OpenCV (cv2)
- **数据库**: SQLite3
- **Python版本**: 3.x

## 项目结构

```
myproject/
├── myproject/           # Django项目配置
│   ├── settings.py      # 项目设置
│   ├── urls.py         # 主URL配置
│   └── wsgi.py         # WSGI配置
├── cv/                 # 计算机视觉应用
│   ├── views.py        # API视图
│   ├── urls.py         # 应用URL配置
│   ├── utils.py        # 图像处理工具函数
│   └── models.py       # 数据模型
├── db.sqlite3          # SQLite数据库
└── manage.py           # Django管理脚本
```

## API 接口

### 1. 图像模板匹配接口

#### 基本信息
- **URL**: `/api/cv`
- **方法**: `POST`
- **功能**: 在目标图像中查找模板图像并返回匹配位置

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| image | File | 是 | 目标图像文件（支持JPG、PNG等格式） |

#### 请求示例

```bash
curl -X POST http://127.0.0.1:8000/api/cv \
  -F "image=@1.jpg"
```

```python
import requests

url = "http://127.0.0.1:8000/api/cv"
files = {'image': open('1.jpg', 'rb')}
response = requests.post(url, files=files)
```

#### 响应格式

**成功响应 (200 OK)**
```json
{
    "success": true,
    "x": 150,
    "y": 200
}
```

**失败响应 (400 Bad Request)**
```json
{
    "error": "没有上传图片"
}
```

**匹配失败响应 (200 OK)**
```json
{
    "success": false,
    "error": "未找到匹配项"
}
```

#### 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| success | Boolean | 是否匹配成功 |
| x | Integer | 匹配位置的平均X坐标（成功时） |
| y | Integer | 匹配位置的平均Y坐标（成功时） |
| error | String | 错误信息（失败时） |

## 算法说明

### 模板匹配算法

1. **图像读取**: 使用OpenCV读取模板图像和目标图像
2. **模板匹配**: 使用`cv2.matchTemplate()`进行模板匹配
3. **阈值过滤**: 设置匹配阈值为0.8，过滤低质量匹配
4. **坐标计算**: 计算所有匹配项的中心坐标
5. **平均坐标**: 计算所有匹配项的平均坐标作为最终结果
6. **结果标记**: 在输出图像上标记匹配区域和平均坐标

### 匹配参数

- **匹配方法**: `cv2.TM_CCOEFF_NORMED` (归一化相关系数)
- **匹配阈值**: 0.8
- **输出文件**: `marked_result.png` (标记后的图像)

## 部署说明

### 环境要求

```bash
pip install django==5.2.7
pip install opencv-python
pip install numpy
```

### 启动服务

```bash
# 进入项目目录
cd myproject

# 运行数据库迁移（首次运行）
python manage.py migrate

# 启动开发服务器
python manage.py runserver
```

### 服务地址

- **开发环境**: http://127.0.0.1:8000
- **API端点**: http://127.0.0.1:8000/api/cv

## 测试说明

### 使用测试脚本

项目根目录提供了 `pa.py` 测试脚本：

```bash
python pa.py
```

### 手动测试

1. 确保Django服务器正在运行
2. 准备测试图片（建议使用项目中的 `1.jpg`）
3. 使用Postman、curl或其他HTTP客户端发送POST请求

## 错误处理

### 常见错误及解决方案

| 错误信息 | 原因 | 解决方案 |
|----------|------|----------|
| "没有上传图片" | 请求中缺少image参数 | 确保使用multipart/form-data格式上传文件 |
| "未找到匹配项" | 模板匹配失败 | 检查图像质量，调整匹配阈值 |
| 连接失败 | Django服务器未启动 | 运行 `python manage.py runserver` |
| 文件读取错误 | 图像文件损坏或格式不支持 | 使用支持的图像格式（JPG、PNG等） |

## 性能说明

- **处理时间**: 通常在1-3秒内完成
- **图像大小**: 建议不超过10MB
- **并发处理**: 开发环境支持单线程处理
- **内存使用**: 根据图像大小动态分配

## 注意事项

1. **文件路径**: 模板图像固定为 `2.png`，输出图像为 `marked_result.png`
2. **图像格式**: 支持常见图像格式，建议使用JPG或PNG
3. **坐标系统**: 使用像素坐标，原点(0,0)位于图像左上角
4. **匹配精度**: 阈值设置为0.8，可根据需要调整

## 更新日志

- **v1.0.0**: 初始版本，支持基本的模板匹配功能
- 支持多匹配项检测和平均坐标计算
- 提供详细的匹配结果可视化

## 联系方式

如有问题或建议，请联系开发团队。

---

*最后更新: 2024年*
