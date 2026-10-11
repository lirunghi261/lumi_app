# Lumi Wallet — kế hoạch UX/UI quản lý chi tiêu

## Mục tiêu trải nghiệm

Người dùng mở ứng dụng và trả lời được ngay bốn câu hỏi:

1. Hiện mình còn bao nhiêu tiền?
2. Tiền đang nằm trong những quỹ nào?
3. Tháng này đã chi bao nhiêu và danh mục nào sắp vượt hạn mức?
4. Hôm nay có điều gì cần chú ý?

Một khoản chi thông thường phải ghi được trong vài thao tác: nhập số tiền, nội dung, chọn quỹ và danh mục.

## Cấu trúc chính

### Tổng quan

- Tổng số dư của tất cả quỹ.
- Quỹ chính và các quỹ phụ.
- Cảnh báo quan trọng từ Lumi.
- Ngân sách theo danh mục.
- Giao dịch gần đây.

### Lịch chi tiêu

- Tổng chi của tháng đang xem.
- Đánh dấu ngày có phát sinh chi tiêu.
- Xem, sửa, xóa giao dịch theo ngày.
- Chọn nhanh tháng và năm cũ.
- Ghi khoản chi cho ngày đang chọn.

### Ghi nhanh

- Quỹ chính được chọn mặc định.
- Có thể đổi sang quỹ phụ ngay trong form.
- Chọn danh mục và ngày ghi nhận.
- Sau khi lưu, số dư quỹ được cập nhật tức thời.

### Ngân sách & Quỹ

- Tổng số dư thực tế.
- Tạo quỹ chính hoặc quỹ phụ.
- Điều chỉnh tên, biểu tượng và số dư.
- Kế hoạch thu nhập và hạn mức tháng.
- Hạn mức từng danh mục.

### Tôi

- Hồ sơ và giao diện.
- Sao lưu, khôi phục và đặt lại dữ liệu.
- Cài đặt thông báo và quyền riêng tư ở giai đoạn tiếp theo.

## Quy tắc dữ liệu quỹ

- Thêm khoản chi: trừ số dư của quỹ được chọn.
- Sửa khoản chi: hoàn tiền về quỹ cũ, sau đó trừ quỹ mới.
- Xóa khoản chi: hoàn tiền về đúng quỹ.
- Giao dịch cũ được gắn vào Quỹ chính khi nâng cấp.
- Quỹ chính là lựa chọn mặc định của Ghi nhanh.

## Hệ thống nhắc nhở

### Đã triển khai trong ứng dụng

- Quỹ bị âm hoặc số dư thấp.
- Tổng ngân sách tháng đã dùng từ 85%.
- Danh mục đã dùng từ 85% hạn mức.
- Trạng thái an toàn khi không có cảnh báo.

### Giai đoạn tiếp theo

- Hóa đơn định kỳ sắp đến hạn.
- Khoản vay hoặc tiền phải trả.
- Ngày chi tiêu bất thường so với thói quen.
- Mục tiêu tiết kiệm bị chậm tiến độ.
- Tùy chọn giờ nhắc và tắt từng loại thông báo để tránh spam.

## Lộ trình ưu tiên

1. Nhiều quỹ và giao dịch theo quỹ — đã hoàn thành nền tảng.
2. Thu nhập, chuyển tiền giữa các quỹ và lịch sử số dư.
3. Hóa đơn định kỳ và thông báo hệ thống.
4. Mục tiêu tiết kiệm có thời hạn.
5. Báo cáo xu hướng tuần/tháng và đề xuất cá nhân hóa.

