import random
import string
from DrissionPage import Chromium, ChromiumOptions
from DrissionPage.common import Keys

def random_string(length):
    return ''.join(random.choices(string.ascii_lowercase + string.digits, k=length))

def random_name():
    first_names = ['Liam', 'Emma', 'Noah', 'Olivia', 'Ava', 'Ethan', 'Sophia', 'Mason', 'Isabella', 'Lucas']
    last_names = ['Smith', 'Johnson', 'Williams', 'Brown', 'Jones', 'Garcia', 'Miller', 'Davis', 'Wilson', 'Taylor']
    return random.choice(first_names), random.choice(last_names)

# 浏览器初始化
co = ChromiumOptions()
browser = Chromium(co)
tab = browser.latest_tab

# 打开 Outlook 注册页面
tab.get('https://www.microsoft.com/zh-cn/microsoft-365/outlook/email-and-calendar-software-microsoft-outlook')
print(tab, "已打开微软官网")

tab2 = tab.ele('创建免费帐户', index=3).click.for_new_tab()
print(tab2, "进入创建帐户页")

tab2.ele('同意并继续').click()

# 生成随机数据
email_prefix = random_string(15)
password = random_string(12)
first_name, last_name = random_name()

# 输入邮箱前缀
tab2.ele('新建电子邮件').input(email_prefix, clear=True)
tab2.ele('t:button@@tx():下一步').click()

# 输入密码
tab2.ele('t:input@@id=floatingLabelInput13').input(password, clear=True)
tab2.ele('下一步').click()

# 出生日期选择
tab2.wait(2)
tab2.actions.move_to('t:label@@tx():年份').click()
tab2.actions.type('1988')

tab2.actions.move_to('月').click()
for _ in range(10):
    tab2.actions.type(Keys.DOWN)
tab2.actions.type(Keys.ENTER)

tab2.actions.move_to('t:label@@tx():日').click()
for _ in range(24):
    tab2.actions.type(Keys.DOWN)
tab2.actions.type(Keys.ENTER)

tab2.ele('下一步').click()

# 输入姓名
tab2.ele('x://input[@id="lastNameInput"]').input(last_name, clear=True)
tab2.ele('t:input@@id=firstNameInput').input(first_name, clear=True)
tab2.ele('x://input[@id="marketingOptIn"]').click()
tab2.ele('t:button@@tx():下一步').click()

tab2.wait(10)

# 检查是否存在 “暂时跳过”
skip_btn = tab2.ele('暂时跳过')
if skip_btn:
    skip_btn.click()
    print("检测到 '暂时跳过' 按钮，跳过验证码。")
else:
    # 开始验证码处理
    while True:
        iframe = tab2.ele('t:iframe@@title=验证质询')
        if not iframe:
            break  # 无验证码则退出
        btn = iframe.ele('#px-captcha')
        iframe.wait(5)
        sr_iframe = btn.sr('t:iframe@@style:display: block')

        pressing_btn = sr_iframe.ele('按住') or sr_iframe.ele('按下')
        if not pressing_btn:
            print("未检测到验证码按钮，重试中...")
            tab2.wait(3)
            continue

        press_time = pressing_btn.attr('style')
        if press_time:
            press_time = press_time.replace('animation: ', '').split('ms')[0]
            press_time = float(press_time) / 1000 + 4
        else:
            press_time = random.uniform(8, 10)
        print(f'需要按压时间：{press_time}秒')

        x, y = random.uniform(-8, 8), random.uniform(-1, 1)
        print(f'偏移量: x={x}, y={y}')
        sr_iframe.actions.move_to(pressing_btn, x, y, 1).hold().wait(press_time).release()
        tab2.wait(10)

        # 检查“暂时跳过”
        skip_btn = tab2.ele('暂时跳过')
        if skip_btn:
            skip_btn.click()
            print("验证码未通过，但检测到 '暂时跳过'，点击跳过。")
            break

# 检查是否注册成功
tab2.wait(10)
current_url = tab2.url
if current_url.startswith('https://outlook.live.com/mail/0/'):
    print("\n✅ 注册成功！")
    print(f"邮箱前缀: {email_prefix}")
    print(f"密码: {password}")
    print(f"姓氏: {last_name}")
    print(f"名字: {first_name}")
else:
    print("\n❌ 注册未完成，当前URL:", current_url)
