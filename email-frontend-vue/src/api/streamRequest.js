/**
 * 流式AI请求工具
 * 使用 XMLHttpRequest progressive reading，兼容 Chrome 49
 * 
 * 用法：
 *   var handle = streamRequest(params, callbacks)
 *   handle.abort()  // 取消请求
 */

/**
 * 对字符串进行URL编码（兼容Chrome 49，不依赖URLSearchParams）
 */
function encodeParams(params) {
  var parts = []
  for (var key in params) {
    if (Object.prototype.hasOwnProperty.call(params, key) && params[key] !== undefined && params[key] !== null) {
      parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]))
    }
  }
  return parts.join('&')
}

/**
 * 流式AI请求
 * @param {Object} params - 请求参数 {method:'streamAgent', capability:'AUTO_REPLY', subject:'', content:''}
 * @param {Object} callbacks - 回调函数集合
 *   - onThinking(chunk): 收到thinking内容片段
 *   - onResult(chunk): 收到result内容片段
 *   - onDone(data): 流式完成，data={thinking, result}
 *   - onError(msg): 发生错误
 * @returns {{ abort: Function }} - 包含abort方法的对象，用于取消请求
 */
function streamRequest(params, callbacks) {
  var onThinking = callbacks.onThinking || function() {}
  var onResult = callbacks.onResult || function() {}
  var onDone = callbacks.onDone || function() {}
  var onError = callbacks.onError || function() {}

  var xhr = new XMLHttpRequest()
  var lastIndex = 0
  var buffer = ''
  var aborted = false
  var firstEventReceived = false

  // 降级超时：5秒内没收到任何SSE事件则降级到同步
  var fallbackTimer = setTimeout(function() {
    if (!firstEventReceived && !aborted) {
      aborted = true
      xhr.abort()
      onError('流式连接超时，请稍后重试')
    }
  }, 5000)

  // 节流更新：合并多个chunk后批量通知Vue
  var pendingThinking = ''
  var pendingResult = ''
  var rafId = null

  function flushPending() {
    if (pendingThinking) {
      onThinking(pendingThinking)
      pendingThinking = ''
    }
    if (pendingResult) {
      onResult(pendingResult)
      pendingResult = ''
    }
    rafId = null
  }

  function scheduleFlush() {
    if (!rafId) {
      // 优先用requestAnimationFrame，不存在则用setTimeout(16ms)
      if (typeof requestAnimationFrame !== 'undefined') {
        rafId = requestAnimationFrame(flushPending)
      } else {
        rafId = setTimeout(flushPending, 16)
      }
    }
  }

  xhr.open('POST', '/api/ai/agent', true)
  xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded')
  xhr.timeout = 120000 // 120秒总超时

  // 确保responseType为text（默认值，不设置也可以）
  // xhr.responseType = ''  // 不设置，保持默认

  xhr.onreadystatechange = function() {
    if (aborted) return

    if (xhr.readyState >= 3) {
      // 获取新增的响应文本
      var fullText = xhr.responseText
      var newText = fullText.substring(lastIndex)
      lastIndex = fullText.length

      if (newText.length > 0) {
        buffer += newText
        processBuffer()
      }
    }

    if (xhr.readyState === 4) {
      clearTimeout(fallbackTimer)
      // 处理buffer中剩余数据
      processBuffer(true)

      if (xhr.status !== 200 && !aborted) {
        onError('请求失败: HTTP ' + xhr.status)
      }

      // 刷新剩余的pending数据
      if (pendingThinking || pendingResult) {
        flushPending()
      }
    }
  }

  xhr.ontimeout = function() {
    clearTimeout(fallbackTimer)
    if (!aborted) {
      aborted = true
      onError('请求超时，请稍后重试')
    }
  }

  xhr.onerror = function() {
    clearTimeout(fallbackTimer)
    if (!aborted) {
      aborted = true
      onError('网络错误，请检查网络连接')
    }
  }

  // 处理buffer中的SSE事件
  function processBuffer(isFinal) {
    var safety = 1000
    while (safety-- > 0) {
      // 查找SSE事件分隔符 \n\n
      var sepIndex = buffer.indexOf('\n\n')
      if (sepIndex === -1) {
        // 如果是最终处理，尝试解析不完整的最后一个事件
        if (isFinal && buffer.trim().length > 0) {
          parseSseBlock(buffer.trim())
          buffer = ''
        }
        break
      }

      var eventBlock = buffer.substring(0, sepIndex)
      buffer = buffer.substring(sepIndex + 2)

      parseSseBlock(eventBlock)
    }
  }

  // 解析单个SSE事件块
  function parseSseBlock(block) {
    if (!block) return

    var lines = block.split('\n')
    for (var i = 0; i < lines.length; i++) {
      var line = lines[i]
      if (line.indexOf('data: ') === 0) {
        var jsonStr = line.substring(6)
        try {
          var data = JSON.parse(jsonStr)
          handleSseData(data)
        } catch (e) {
          // 解析失败，可能是 [DONE] 或非JSON
          if (jsonStr.trim() === '[DONE]') {
            // 流结束标记，正常完成
          }
        }
      }
    }
  }

  // 处理解析后的SSE数据
  function handleSseData(data) {
    if (!firstEventReceived) {
      firstEventReceived = true
      clearTimeout(fallbackTimer) // 收到第一个事件，取消降级超时
    }

    var type = data.type
    if (type === 'thinking' && data.content) {
      pendingThinking += data.content
      scheduleFlush()
    } else if (type === 'result' && data.content) {
      pendingResult += data.content
      scheduleFlush()
    } else if (type === 'done') {
      // 流式完成，先刷新pending
      if (pendingThinking || pendingResult) {
        flushPending()
      }
      onDone(data)
    } else if (type === 'error') {
      onError(data.message || 'AI服务错误')
    }
  }

  // 发送请求
  var body = encodeParams(params)
  xhr.send(body)

  // 返回abort句柄
  return {
    abort: function() {
      aborted = true
      clearTimeout(fallbackTimer)
      if (rafId) {
        if (typeof cancelAnimationFrame !== 'undefined') {
          cancelAnimationFrame(rafId)
        } else {
          clearTimeout(rafId)
        }
      }
      xhr.abort()
    }
  }
}

export default streamRequest
