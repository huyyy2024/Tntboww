# Fire Arrow TNT Mod

Fabric client-side mod cho Minecraft 1.21.
Bắn tên cháy → tự động đặt Rail + TNT Minecart tại điểm va chạm.

---

## Cách dùng trên điện thoại (không cần máy tính)

### Bước 1 — Tạo repo GitHub
1. Mở [github.com](https://github.com) trên trình duyệt điện thoại
2. Đăng nhập → bấm **+** → **New repository**
3. Đặt tên repo, chọn **Public**, bấm **Create repository**

### Bước 2 — Upload từng file
Trên trang repo vừa tạo, bấm **Add file → Upload files**  
Upload toàn bộ file trong zip này, giữ đúng cấu trúc thư mục:
```
.github/workflows/build.yml
build.gradle
gradle.properties
settings.gradle
src/main/java/com/firebow/...
src/main/resources/...
```

> GitHub không cho upload thư mục trên điện thoại trực tiếp.  
> Dùng app **GitLab** hoặc **Spck Editor** để push đúng cấu trúc,  
> hoặc dùng **GitHub Codespaces** (xem bên dưới).

### Bước 3 — Dùng GitHub Codespaces (cách dễ nhất)
1. Vào repo → bấm nút **Code** → tab **Codespaces** → **Create codespace**
2. Chờ terminal mở (chạy trên trình duyệt)
3. Kéo thả file zip vào cửa sổ Codespaces để upload
4. Chạy trong terminal:
   ```bash
   unzip fire-arrow-tnt-mod-fixed.zip
   cp -r firemod/. .
   rm -rf firemod
   git add .
   git commit -m "add mod files"
   git push
   ```
5. Đóng Codespaces — GitHub Actions tự chạy build

### Bước 4 — Tải JAR
1. Vào tab **Actions** trên repo
2. Chọn run mới nhất → kéo xuống **Artifacts**
3. Tải **fire-arrow-tnt-jar**

### Bước 5 — Cài vào PojavLauncher
Copy file `.jar` vào:
```
/sdcard/games/PojavLauncher/mods/
```
Cần có thêm `fabric-api-0.100.4+1.21.jar` trong cùng thư mục.

---

## Yêu cầu khi chơi
- Hotbar phải có **Rail** hoặc **Powered Rail**
- Hotbar phải có **TNT Minecart**
- Cung phải có enchant **Flame**
