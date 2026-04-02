#!/usr/bin/env python3
"""JimuReport Enhancement API Test Script"""

import requests
import json
import sys

BASE_URL = "http://localhost:8085"

def test_login():
    """Test login endpoint"""
    print("1. 测试登录接口...")
    try:
        response = requests.post(f"{BASE_URL}/api/user/login", json={
            "username": "admin",
            "password": "admin123"
        }, timeout=10)
        
        if response.status_code == 200:
            data = response.json()
            if "token" in data:
                print(f"✅ 登录成功，获取到token: {data['token'][:50]}...")
                return data["token"]
            else:
                print(f"❌ 登录响应缺少token: {data}")
                return None
        else:
            print(f"❌ 登录失败，状态码: {response.status_code}")
            print(f"   响应: {response.text[:200]}")
            return None
    except Exception as e:
        print(f"❌ 登录请求异常: {e}")
        return None

def test_endpoints(token):
    """Test all API endpoints"""
    headers = {"Authorization": f"Bearer {token}"}
    
    endpoints = [
        ("GET", "/api/user/current", "获取当前用户信息"),
        ("GET", "/api/user/list", "获取用户列表"),
        ("GET", "/api/department/list", "获取部门列表"),
        ("GET", "/api/department/tree", "获取部门树"),
        ("GET", "/api/role/list", "获取角色列表"),
        ("GET", "/api/menu/list", "获取菜单列表"),
        ("GET", "/api/menu/tree", "获取菜单树"),
        ("GET", "/api/permission/current/permissions", "获取当前用户权限"),
        ("GET", "/api/permission/current/roles", "获取当前用户角色"),
        ("GET", "/api/permission/current/menus", "获取当前用户菜单"),
    ]
    
    print("\n2. 测试API端点...")
    for method, path, desc in endpoints:
        try:
            if method == "GET":
                response = requests.get(f"{BASE_URL}{path}", headers=headers, timeout=10)
            else:
                continue
                
            if response.status_code == 200:
                data = response.json()
                if isinstance(data, list):
                    print(f"✅ {desc}: 返回 {len(data)} 条记录")
                elif isinstance(data, dict):
                    if "total" in data:
                        print(f"✅ {desc}: 共 {data['total']} 条记录")
                    elif "username" in data:
                        print(f"✅ {desc}: 用户名 {data['username']}")
                    else:
                        print(f"✅ {desc}: 成功")
                else:
                    print(f"✅ {desc}: 成功")
            else:
                print(f"❌ {desc}: 状态码 {response.status_code}")
        except Exception as e:
            print(f"❌ {desc}: 异常 {e}")

def test_jimureport(token):
    """Test JimuReport endpoints"""
    print("\n3. 测试JimuReport接口...")
    
    # Test JimuReport list page
    try:
        response = requests.get(f"{BASE_URL}/jmreport/list", timeout=10)
        if response.status_code == 200:
            print("✅ 报表列表页面可访问")
        else:
            print(f"❌ 报表列表页面不可访问: {response.status_code}")
    except Exception as e:
        print(f"❌ 报表列表页面异常: {e}")
    
    # Test JimuReport with token
    try:
        response = requests.get(f"{BASE_URL}/jmreport/list?token={token}", timeout=10)
        if response.status_code == 200:
            print("✅ 报表token验证成功")
        else:
            print(f"❌ 报表token验证失败: {response.status_code}")
    except Exception as e:
        print(f"❌ 报表token验证异常: {e}")

def test_swagger():
    """Test Swagger endpoints"""
    print("\n4. 测试Swagger接口...")
    
    try:
        response = requests.get(f"{BASE_URL}/swagger-ui.html", timeout=10)
        if response.status_code == 200:
            print("✅ Swagger UI可访问")
        else:
            print(f"❌ Swagger UI不可访问: {response.status_code}")
    except Exception as e:
        print(f"❌ Swagger UI异常: {e}")

def test_actuator():
    """Test Actuator endpoints"""
    print("\n5. 测试Actuator接口...")
    
    try:
        response = requests.get(f"{BASE_URL}/actuator/health", timeout=10)
        if response.status_code == 200:
            print("✅ Actuator健康检查正常")
        else:
            print(f"❌ Actuator健康检查失败: {response.status_code}")
    except Exception as e:
        print(f"❌ Actuator健康检查异常: {e}")

def main():
    print("=== JimuReport Enhancement API 测试 ===\n")
    
    # Test login
    token = test_login()
    if not token:
        print("\n❌ 登录失败，无法继续测试")
        sys.exit(1)
    
    # Test other endpoints
    test_endpoints(token)
    test_jimureport(token)
    test_swagger()
    test_actuator()
    
    print("\n=== 测试完成 ===")

if __name__ == "__main__":
    main()
