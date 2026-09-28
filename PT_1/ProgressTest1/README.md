# SWT301 – ProgressTest 1: Unit Testing

## 1. Thông tin sinh viên & Cách chạy
- **MSSV:** DE200357
- **Cách chạy kiểm thử:**
    - Chạy toàn bộ test qua Maven: `mvn clean test`
    - Báo cáo JaCoCo xuất tại: `target/site/jacoco/index.html`

---

## 2. Báo cáo kiểm thử Mutation Testing (3 lỗi giả lập)

| # | File / Vị trí | Lỗi chèn (Mutation) | Test bắt được lỗi (Failed Test) | Trạng thái hoàn tác |
|---|---|---|---|---|
| M1 | `AccountService.java` | Sửa `>= MAX_FAILED_ATTEMPTS` thành `> MAX_FAILED_ATTEMPTS` | `Login.login_FifthFailedAttempt_LocksAccount` | Đã hoàn tác |
| M2 | `AccountService.java` | Bỏ qua nhánh `if (acc.isLocked())` | `Login.login_AlreadyLocked_DoesNotIncreaseCounter` | Đã hoàn tác |
| M3 | `AccountValidator.java` | Sửa regex USERNAME `{4,19}` thành `{4,20}` | `AccountValidatorTest.isValidUsername_BoundaryLength` (độ dài 21) | Đã hoàn tác |

---

## 3. Báo cáo độ bao phủ JaCoCo (Coverage Summary)
- **AccountValidator:** Line ≥ 80%, Branch ≥ 70%
- **AccountService:** Line ≥ 80%, Branch ≥ 70%

---

## 4. Ma trận truy vết rút gọn (Traceability Matrix: BR -> Tên Test)

| Business Rule | Mô tả quy tắc | Tên Test Case tương ứng |
|---|---|---|
| **BR-REG-01** | Bắt buộc nhập, DOB không ở tương lai | `invalidRegisterInputs` (case null, dob tương lai) |
| **BR-REG-02** | Định dạng username (5-20 ký tự, bắt đầu bằng chữ) | `isValidUsername_Partitions`, `isValidUsername_BoundaryLength` |
| **BR-REG-03** | Trùng username (không phân biệt hoa thường) | `register_DuplicateUsername_CaseInsensitive` |
| **BR-REG-04** | Định dạng email chuẩn, tối đa 100 ký tự | `isValidEmail_Partitions`, `isValidEmail_BoundaryLength` |
| **BR-REG-05** | Trùng email (không phân biệt hoa thường) | `register_DuplicateEmail_CaseInsensitive` |
| **BR-REG-06** | Mật khẩu mạnh (8-32 ký tự, đủ 4 nhóm, không chứa user) | `isValidPassword_Partitions`, `isValidPassword_BoundaryLength` |
| **BR-REG-07** | Xác nhận mật khẩu khớp | `invalidRegisterInputs` (case PASSWORD_MISMATCH) |
| **BR-REG-08** | Tuổi tối thiểu >= 18 tuổi | `register_AgeBoundary`, `calculateAge_Boundaries` |
| **BR-REG-09** | Định dạng số điện thoại (tùy chọn) | `isValidPhone_ValidPrefixes`, `isValidPhone_InvalidFormats` |
| **BR-REG-10** | Sinh salt ngẫu nhiên và băm SHA-256 | `register_ValidData_SuccessAndStateCorrect` |
| **BR-LOG-01** | Kiểm tra username/password không được rỗng | `Login.login_BlankInput_ReturnsInvalidInput` |
| **BR-LOG-02** | User không tồn tại -> INVALID_CREDENTIALS | `Login.login_UserNotFound_ReturnsInvalidCredentials` |
| **BR-LOG-03** | Tài khoản DISABLED -> ACCOUNT_DISABLED | `Login.login_DisabledAccount_ReturnsAccountDisabled` |
| **BR-LOG-04** | Sai <= 4 lần: tăng bộ đếm, không khóa | `Login.login_FailedAttemptsUnderThreshold_ReturnsInvalidCredentials` |
| **BR-LOG-05** | Sai lần thứ 5: khóa tài khoản | `Login.login_FifthFailedAttempt_LocksAccount` |
| **BR-LOG-06** | Đăng nhập thành công: reset bộ đếm về 0 | `Login.login_CorrectCredentials_SuccessAndResetsCounter` |
| **BR-ADM-03** | Admin mở khóa, đặt lại số lần sai = 0 | `Admin.unlockAccount_ValidUser_UnlocksAndResetsCounter` |