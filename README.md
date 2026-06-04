# Pure iPerf3 (纯粹 iPerf3)

[English](#english) | [中文](#chinese)

---

<a name="english"></a>
## English

**Pure iPerf3** is a lightweight, ad-free, and fully open-source Android wrapper for the industry-standard network utility `iperf3`. Built using modern Android architecture, it enables you to perform standard network bandwidth testing in both client and server modes directly from your mobile device.

---

### 🌟 Features

* **Ad-Free & Clean UI:** A modern, minimalist user interface designed using Jetpack Compose and Material 3 design systems.
* **Dual Modes Support:**
  * **Client Mode:** Run tests against remote iPerf3 servers. Fully customize the port, direction (upload/download), and parallel streams (1 to 64 threads).
  * **Server Mode:** Run your Android device as an iPerf3 server listening on a custom port to test throughput from other devices.
* **TCP / UDP Protocol Toggle:** 
  * Select between TCP and UDP protocols for testing.
  * **Custom UDP Bandwidth Limit:** Allows setting custom UDP bandwidth limits (e.g. `0` for unlimited, `10M`, `100M`) to easily test UDP network capacity and bypass the default 1 Mbps limit.
* **Adjustable Test Duration:** Fine-tune test duration (iterations) between `1s` and `120s` with a precise `1s` step size (default is `10s`).
* **Real-time Visualizations:**
  * Displays real-time bandwidth (Mbps) dynamically during active tests.
  * Canvas-based chart visualizing bandwidth fluctuations.
  * Detailed real-time interval table showing second-by-second throughput.
* **Local Test History & Interval Details Popup:** 
  * Automatically persists test results into a local Room database.
  * Show records containing host, port, average bandwidth, peak bandwidth, minimum bandwidth, thread count, protocol (with UDP limit details), test duration, and timestamp.
  * **Clickable History Details Dialog:** Tap any history card to view a scrollable list of second-by-second throughput intervals, peak speed, minimum speed, average speed, and timestamp.
* **Advanced Error Handling & Translation:** Detects common socket failures (connection refused, timeout, reset, stream read/write issues, broken pipe) and translates them into user-friendly Chinese when the system locale is set to Chinese.
* **Localization:** Supports English and Simplified Chinese (auto-detects system locales, with manual dropdown switches).

---

### 🛠️ Architecture Details

* **UI Framework:** Jetpack Compose (Declarative UI) with Material 3.
* **Architecture Pattern:** MVVM (Model-View-ViewModel) using Kotlin Coroutines and Flows for state management.
* **Local Storage:** Room Database (SQLite abstraction) version 4 with destructive migration fallback for storing test records.
* **Core Speed Engine:** The application invokes a native compiled static `iperf3` binary (`libiperf3.so`) packaged within `jniLibs`. The wrapper uses Java `ProcessBuilder` to start the engine and reads standard output in real-time.
  * **NDK/Bionic Static Binaries:** Uses binaries precompiled for the Android NDK environment (compiled from `davidBar-On/android-iperf3` source, supporting version `3.21`), avoiding linking errors on Android libc (Bionic) that occur when using standard Linux static binaries.
  * **Packaging and Executables:** Supports `arm64-v8a`, `armeabi-v7a`, `x86_64`, and `x86`. Native library extraction is forced (`android:extractNativeLibs="true"` in Manifest) to ensure the binary runs directly from the Android filesystem.

---

### 🚀 Build & Compilation Instructions

#### Prerequisites
* **JDK 17 or Java 24** installed.
* **Android SDK** installed (Platform API 36 or higher).
* Local Android SDK path configured in `local.properties` (e.g. `sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk`).

#### 1. Compiling Debug APK
In the project root, run the Gradle wrapper command to output a Debug APK:
* **Windows (PowerShell/CMD):**
  ```powershell
  .\gradlew.bat assembleDebug
  ```
* **Linux / macOS (Bash/Zsh):**
  ```bash
  ./gradlew assembleDebug
  ```
* **Output Path:** `app/build/outputs/apk/debug/app-debug.apk`

#### 2. Compiling Signed Release APK
The project contains a pre-configured keystore configuration in `app/build.gradle.kts` referencing a self-signed key (`my-upload-key.jks`) located in the project root.
* **Windows (PowerShell/CMD):**
  ```powershell
  .\gradlew.bat assembleRelease
  ```
* **Linux / macOS (Bash/Zsh):**
  ```bash
  ./gradlew assembleRelease
  ```
* **Output Path:** `app/build/outputs/apk/release/app-release.apk`

---

<a name="chinese"></a>
## 中文

**Pure iPerf3** 是一款轻量、无广告且完全开源的 Android 网速测试工具。它基于行业标准的网络测试工具 `iperf3` 开发，并采用现代安卓技术栈进行封装。支持在移动设备上以**客户端（Client）**或**服务端（Server）**模式运行，方便您评估局域网或广域网吞吐量性能。

---

### 🌟 核心功能

* **纯净无广告**：基于 Jetpack Compose 与 Material 3 设计语言构建的现代扁平化界面，绝无广告打扰。
* **双模式测速**：
  * **客户端模式**：向远程 iPerf3 服务端发起测速，支持配置自定义端口、测试方向（上传/下载）以及多线程并发数（支持 1 - 64 线程）。
  * **服务端模式**：在手机端开启 iPerf3 监听服务，无需指定协议类型，自动接受来自局域网内其他 TCP/UDP 客户端的连接。
* **TCP / UDP 协议切换与 UDP 带宽限制**：
  * 支持自由切换 TCP 和 UDP 协议。
  * **UDP 带宽限制调节**：iPerf3 默认 UDP 速率仅有 1 Mbps，应用提供带宽限制输入框（支持输入 `0` 表示不限速，或 `10M`、`100M`、`1G` 等标准单位进行吞吐量压测）。
* **单次测试时长（次数）精细化调节**：
  * 支持以 `1s` 为步长精细调节测试时间 `1s - 120s`（默认为 `10s`，即测量获取 10 次区间速率数据）。
* **实时图表与数据看板**：
  * 实时显示当前带宽速度（Mbps）。
  * 基于 Canvas 绘制的动态实时带宽波动曲线图。
  * 实时展示每秒测速区间（Interval）的具体吞吐速度和传输速率。
* **本地历史记录与详情弹窗**：
  * 使用本地 Room 数据库自动保存每一次的测速结果。
  * 记录内容包括：测试端模式、上传/下载方向、端口、目标主机、并发线程数、协议（带UDP限速值）、测试时长、平均带宽、峰值带宽、最低带宽及测速时间。
  * **可点击的历史记录详情弹窗**：点击历史记录卡片，会以优雅的垂直布局（大字体数字，配合下方 Mbps 单位标签）清晰显示平均、峰值和最低带宽，并提供可滚动的每秒区间（0.00-1.00s 等）具体传输数据。
* **异常与手动停止报错拦截**：
  * 增加了针对 `Connection reset by peer`、`broken pipe` 或控制套接字提前关闭等异常网络中断的完全中文翻译提示。
  * 优化了手动点击“停止测速”的逻辑，提前取消协程 Job，完美拦截并屏蔽因手动终止 iperf3 进程导致的 Java 流关闭异常，避免报错横幅干扰界面。

---

### 🛠️ 技术架构

* **界面框架**：Jetpack Compose (声明式 UI) 与 Material Design 3 风格。
* **架构模式**：MVVM 架构，使用 Kotlin 协程 (Coroutines) 与数据流 (Flows) 进行响应式状态管理。
* **数据持久化**：使用 Room 数据库版本 4，并开启了破坏性迁移机制。
* **核心测速引擎**：
  * **Android 专有静态编译二进制文件**：项目在 `jniLibs` 中内置了专为 Android 编译的静态 iPerf3 二进制文件（支持 3.21，源自 `davidBar-On/android-iperf3`），从而解决了标准 Linux 静态二进制文件在 Android libc (Bionic) 环境下的动态链接器/执行格式兼容性错误。
  * **进程调用机制**：业务层通过 Java 的 `ProcessBuilder` 启动该进程，通过管道读取标准输出流，并在协程中实时解析出瞬时带宽数据。在清单文件中声明了 `android:extractNativeLibs="true"`，强迫系统在安装时将 `libiperf3.so` 解压为具备可执行权限的物理文件。

---

### 🚀 编译与构建指南

#### 环境准备
* 已经安装 **JDK 17 或 Java 24**。
* 已经安装 **Android SDK**（支持平台 API 36 或以上）。
* 在本地 `local.properties` 中正确配置您的 Android SDK 路径。

#### 1. 编译 Debug 测试包 (APK)
在项目根目录下打开终端，执行构建指令：
* **Windows 系统 (PowerShell 或 CMD)：**
  ```powershell
  .\gradlew.bat assembleDebug
  ```
* **Linux 或 macOS 系统 (Terminal)：**
  ```bash
  ./gradlew assembleDebug
  ```
* **生成的 APK 路径：** `app/build/outputs/apk/debug/app-debug.apk`

#### 2. 编译正式签名发布包 (Release APK)
项目已在 `app/build.gradle.kts` 中配置好发布版签名，将使用根目录下的自签名密钥 `my-upload-key.jks`（别名：`upload`，密码：`android`）进行打包：
* **Windows 系统 (PowerShell 或 CMD)：**
  ```powershell
  .\gradlew.bat assembleRelease
  ```
* **Linux 或 macOS 系统 (Terminal)：**
  ```bash
  ./gradlew assembleRelease
  ```
* **生成的 APK 路径：** `app/build/outputs/apk/release/app-release.apk`

---

## 📄 License & Attribution

* **Pure iPerf3 UI Wrapper:** Distributed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for more information.
* **iPerf3 Core:** `iperf3` is licensed under the **3-clause BSD License**. It is compiled statically and packaged as-is. All credits belong to the [ESnet iPerf3 development team](https://github.com/esnet/iperf3).
* **Android Native iPerf3 Binary:** Precompiled for Android Bionic/NDK environments, sourced from the [davidBar-On/android-iperf3 repository](https://github.com/davidBar-On/android-iperf3) under the BSD License.
