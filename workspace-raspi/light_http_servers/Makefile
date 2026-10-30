# コンパイラとフラグの設定
CXX      := g++
#CXXFLAGS := -std=c++23 -Wall -Wextra -Wpedantic -O0 -g -fsanitize=address -pthread
#LDFLAGS  := -fsanitize=address -pthread

CXXFLAGS := -std=c++23 -Wall -Wextra -Wpedantic -O0 -g -pthread
LDFLAGS  := -pthread

# 出力ファイル名とソースファイル
TARGET   := light_http_server
#SRCS     := http.cpp constDef.cpp com_lib.cpp serverConfig.cpp
SRCS     := src/http.cpp src/constDef.cpp src/com_lib.cpp src/serverConfig.cpp

# ソースファイルからオブジェクトファイル（.o）のリストを自動生成
OBJS     := $(SRCS:.cpp=.o)

# デフォルトターゲット
all: $(TARGET)

# リンク処理（オブジェクトファイルから実行ファイルを生成）
$(TARGET): $(OBJS)
	$(CXX) $(OBJS) -o $(TARGET) $(LDFLAGS)

# コンパイル処理（各.cppファイルから.oファイルを生成）
%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $< -o $@

# 生成されたファイルを削除するクリーンアップルール
clean:
	#rm -f $(TARGET) $(OBJS)
	rm -f $(OBJS)

.PHONY: all clean

