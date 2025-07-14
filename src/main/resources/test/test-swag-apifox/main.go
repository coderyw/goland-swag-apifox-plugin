// @title GCash Payment API
// @version 1.0
// @description GCash支付和提现相关API服务
// @termsOfService http://swagger.io/terms/

// @contact.name API Support
// @contact.url http://www.swagger.io/support
// @contact.email support@swagger.io

// @license.name Apache 2.0
// @license.url http://www.apache.org/licenses/LICENSE-2.0.html

// @host localhost:8080
// @BasePath /api/v1
// @schemes http https

// @securityDefinitions.apikey BearerAuth
// @in header
// @name Authorization
// @description Type "Bearer" followed by a space and JWT token.

package main

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
	_ "h/docs" // 这里导入生成的docs包
)

// @Summary 获取用户余额
// @Description 获取指定用户的GCash账户余额
// @Tags 账户管理
// @Accept json
// @Produce json
// @Param user_id path int true "用户ID"
// @Success 200 {object} BalanceResponse
// @Failure 400 {object} ErrorResponse
// @Failure 404 {object} ErrorResponse
// @Failure 500 {object} ErrorResponse
// @Security BearerAuth
// @Router /balance/{user_id} [get]
func getBalance(c *gin.Context) {
	userID := c.Param("user_id")
	// 实现获取余额的逻辑
	c.JSON(http.StatusOK, BalanceResponse{
		UserID:  userID,
		Balance: 1000.50,
		Currency: "PHP",
	})
}

// @Summary 发起支付
// @Description 发起GCash支付交易
// @Tags 支付
// @Accept json
// @Produce json
// @Param payment body PaymentRequest true "支付请求"
// @Success 200 {object} PaymentResponse
// @Failure 400 {object} ErrorResponse
// @Failure 500 {object} ErrorResponse
// @Security BearerAuth
// @Router /payment [post]
func createPayment(c *gin.Context) {
	var req PaymentRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, ErrorResponse{
			Error:   "Invalid request",
			Message: err.Error(),
		})
		return
	}
	
	// 实现支付逻辑
	c.JSON(http.StatusOK, PaymentResponse{
		TransactionID: "TXN123456789",
		Status:        "pending",
		Amount:        req.Amount,
		Currency:      req.Currency,
	})
}

// @Summary 获取交易状态
// @Description 查询指定交易的当前状态
// @Tags 交易查询
// @Accept json
// @Produce json
// @Param transaction_id path string true "交易ID"
// @Success 200 {object} TransactionStatus
// @Failure 400 {object} ErrorResponse
// @Failure 404 {object} ErrorResponse
// @Security BearerAuth
// @Router /transaction/{transaction_id} [get]
func getTransactionStatus(c *gin.Context) {
	transactionID := c.Param("transaction_id")
	
	// 实现查询交易状态的逻辑
	c.JSON(http.StatusOK, TransactionStatus{
		TransactionID: transactionID,
		Status:        "completed",
		Amount:        500.00,
		Currency:      "PHP",
		CreatedAt:     "2024-01-15T10:30:00Z",
		UpdatedAt:     "2024-01-15T10:35:00Z",
	})
}

// @Summary 提现申请
// @Description 申请从GCash账户提现到银行账户
// @Tags 提现
// @Accept json
// @Produce json
// @Param withdrawal body WithdrawalRequest true "提现请求"
// @Success 200 {object} WithdrawalResponse
// @Failure 400 {object} ErrorResponse
// @Failure 500 {object} ErrorResponse
// @Security BearerAuth
// @Router /withdrawal [post]
func createWithdrawal(c *gin.Context) {
	var req WithdrawalRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, ErrorResponse{
			Error:   "Invalid request",
			Message: err.Error(),
		})
		return
	}
	
	// 实现提现逻辑
	c.JSON(http.StatusOK, WithdrawalResponse{
		WithdrawalID: "WD123456789",
		Status:       "processing",
		Amount:       req.Amount,
		Currency:     req.Currency,
		BankAccount:  req.BankAccount,
	})
}

func main() {
	r := gin.Default()
	
	// Swagger文档路由
	r.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
	
	// API路由
	api := r.Group("/api/v1")
	{
		api.GET("/balance/:user_id", getBalance)
		api.POST("/payment", createPayment)
		api.GET("/transaction/:transaction_id", getTransactionStatus)
		api.POST("/withdrawal", createWithdrawal)
	}
	
	r.Run(":8080")
}

// 数据模型定义

// BalanceResponse 余额响应
type BalanceResponse struct {
	UserID   string  `json:"user_id" example:"12345"`
	Balance  float64 `json:"balance" example:"1000.50"`
	Currency string  `json:"currency" example:"PHP"`
}

// PaymentRequest 支付请求
type PaymentRequest struct {
	Amount   float64 `json:"amount" binding:"required" example:"500.00"`
	Currency string  `json:"currency" binding:"required" example:"PHP"`
	Merchant string  `json:"merchant" binding:"required" example:"Shop123"`
	OrderID  string  `json:"order_id" binding:"required" example:"ORD123456"`
}

// PaymentResponse 支付响应
type PaymentResponse struct {
	TransactionID string  `json:"transaction_id" example:"TXN123456789"`
	Status        string  `json:"status" example:"pending"`
	Amount        float64 `json:"amount" example:"500.00"`
	Currency      string  `json:"currency" example:"PHP"`
}

// TransactionStatus 交易状态
type TransactionStatus struct {
	TransactionID string `json:"transaction_id" example:"TXN123456789"`
	Status        string `json:"status" example:"completed"`
	Amount        float64 `json:"amount" example:"500.00"`
	Currency      string `json:"currency" example:"PHP"`
	CreatedAt     string `json:"created_at" example:"2024-01-15T10:30:00Z"`
	UpdatedAt     string `json:"updated_at" example:"2024-01-15T10:35:00Z"`
}

// WithdrawalRequest 提现请求
type WithdrawalRequest struct {
	Amount      float64 `json:"amount" binding:"required" example:"1000.00"`
	Currency    string  `json:"currency" binding:"required" example:"PHP"`
	BankAccount string  `json:"bank_account" binding:"required" example:"1234567890"`
	BankName    string  `json:"bank_name" binding:"required" example:"BDO"`
}

// WithdrawalResponse 提现响应
type WithdrawalResponse struct {
	WithdrawalID string  `json:"withdrawal_id" example:"WD123456789"`
	Status       string  `json:"status" example:"processing"`
	Amount       float64 `json:"amount" example:"1000.00"`
	Currency     string  `json:"currency" example:"PHP"`
	BankAccount  string  `json:"bank_account" example:"1234567890"`
}

// ErrorResponse 错误响应
type ErrorResponse struct {
	Error   string `json:"error" example:"Bad Request"`
	Message string `json:"message" example:"Invalid request parameters"`
}