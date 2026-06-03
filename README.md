# Pure iPerf3 (纯粹 iPerf3)

[English](#english) | [中文](#chinese)

---

<a name="english"></a>
## English

**Pure iPerf3** is a lightweight, ad-free, and fully open-source Android wrapper for the industry-standard network utility `iperf3`. Built using modern Android architecture, it enables you to perform standard network bandwidth testing in both client and server modes directly from your mobile device.

### 🌟 Features
* **Ad-Free & Clean UI:** A modern, minimalist user interface designed using Jetpack Compose and Material 3 design systems.
* **Dual Modes Support:**
  * **Client Mode:** Run tests against remote iPerf3 servers. Fully customize the port, direction (upload/download), and parallel streams (1 to 64 threads).
  * **Server Mode:** Run your Android device as an iPerf3 server listening on a custom port to test throughput from other devices.
* **Real-time Performance Visualizations:**
  * Displays real-time bandwidth (Mbps) dynamically during active tests.
  * Real-time Canvas-based chart visualizing bandwidth fluctuations.
  * Detailed interval log detailing second-by-second throughput.
* **Local Test History:** 
  * Automatically saves test results into a local Room database.
  * Displays historical test records (type, target host, port, timestamp, average bandwidth).
  * Ability to clear history with a single tap.
* **Localization:** Supports English and Simplified Chinese (auto-detects system locales, with manual dropdown switches).

### 🛠️ Architecture Details
* **UI Framework:** Jetpack Compose (Declarative UI) with Material 3.
* **Architecture Pattern:** MVVM (Model-View-ViewModel) using Kotlin Coroutines and Flows for state management.
* **Local Storage:** Room Database (SQLite abstraction) for history persistence.
* **Core Speed Engine:** The application invokes a native compiled static `iperf3` binary (`libiperf3.so`) packaged within `jniLibs`. The wrapper uses Java `ProcessBuilder` to start the engine, reads stdout streams in real-time, and uses robust regular expressions to parse interval bandwidth stats.
* **Version Management:** Gradle Version Catalog (`libs.versions.toml`) with Kotlin DSL for cleaner build scripts.

---

### 🚀 Build & Compilation Instructions

#### Prerequisites
* **JDK 17 or higher** installed.
* **Android SDK** installed (Platform API 36 or higher).

#### Step-by-Step Build Guide
1. Clone the repository to your local system.
2. In the project root, build the project and output a Debug APK:
   * **Windows (PowerShell/CMD):**
     ```powershell
     .\gradlew.bat assembleDebug
     ```
   * **Linux / macOS (Bash/Zsh):**
     ```bash
     ./gradlew assembleDebug
     ```
3. Locate your compiled APK at:
   `app/build/outputs/apk/debug/app-debug.apk`

---

<a name="chinese"></a>
## 中文

**Pure iPerf3** 是一款轻量、无广告且完全开源的 Android 网速测试工具。它基于行业标准的网络测试工具 `iperf3` 开发，并采用现代安卓技术栈进行封装。支持在移动设备上以**客户端（Client）**或**服务端（Server）**模式运行，方便您评估局域网或广域网吞吐量性能。

### 🌟 核心功能
* **纯净无广告**：基于 Jetpack Compose 与 Material 3 设计语言构建的现代扁平化界面，绝无广告打扰。
* **双模式测速**：
  * **客户端模式**：向远程 iPerf3 服务端发起测速，支持配置自定义端口、测试方向（上传/下载）以及多线程并发数（支持 1 - 64 线程）。
  * **服务端模式**：在手机端开启 iPerf3 监听服务，允许局域网内的其他设备向手机发起吞吐量测试。
* **实时图表与数据看板**：
  * 实时显示当前带宽速度（Mbps）。
  * 基于 Canvas 绘制的动态实时带宽波动曲线图。
  * 实时展示每秒测速区间（Interval）的具体吞吐速度。
* **本地历史记录**：
  * 使用本地 Room 数据库自动保存每一次的测速结果。
  * 提供历史测速面板，展示测速类型、目标主机与端口、测速时间以及平均速率。
  * 支持一键清空历史数据。
* **国际化多语言**：支持英文和简体中文，根据系统语言自动切换，并提供顶部手动切换下拉菜单。

### 🛠️ 技术架构
* **界面框架**：Jetpack Compose (声明式 UI) 与 Material Design 3 风格。
* **架构模式**：MVVM 架构，使用 Kotlin 协程 (Coroutines) 与数据流 (Flows) 进行响应式状态管理。
* **数据持久化**：Room 数据库用于持久化存储本地历史记录。
* **核心测速引擎**：应用将静态编译的 native `iperf3` 二进制文件重命名为 `libiperf3.so` 并打包于 `jniLibs` 中。业务层通过 Java 的 `ProcessBuilder` 启动该进程，通过管道读取标准输出流，并基于高精度的正则表达式实时解析出瞬时带宽数据。
* **构建管理**：使用 Gradle Version Catalog (`libs.versions.toml`) 结合 Kotlin DSL 集中管理依赖版本。

---

### 🚀 编译与构建指南

#### 环境准备
* 已经安装 **JDK 17 或更高版本**。
* 已经安装 **Android SDK**（支持平台 API 36 或以上）。

#### 编译打包步骤
1. 克隆或下载本项目至本地。
2. 在项目根目录下打开终端，执行构建指令以生成 Debug 测试包 (APK)：
   * **Windows 系统 (PowerShell 或 CMD)：**
     ```powershell
     .\gradlew.bat assembleDebug
     ```
   * **Linux 或 macOS 系统 (Terminal)：**
     ```bash
     ./gradlew assembleDebug
     ```
3. 编译完成后，您可以在以下路径找到生成的 APK 安装文件：
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 License & Attribution

* **Pure iPerf3 UI Wrapper:** Distributed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for more information.
* **iPerf3 Core:** `iperf3` is licensed under the **3-clause BSD License**. It is compiled statically and packaged as-is. All credits belong to the [ESnet iPerf3 development team](https://github.com/esnet/iperf3).
