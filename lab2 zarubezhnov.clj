(ns your-namespace
  (:require [clojure.core.async :as async]
            [clojure.java.io :as io]))

(defn file->char-channel [file-path]
  (let [ch (async/chan)]
    (Thread/start
      (fn []
        (with-open [reader (io/reader file-path)]
          (doseq [char (line-seq reader)]
            (async/>! ch char)))
        (async/close! ch)))
    ch))

(defn char-channel->string-channel [char-channel]
  (let [ch (async/chan)]
    (async/go
      (loop [result ""]
        (when-let [char (async/<! char-channel)]
          (if (nil? char)
            (async/close! ch)
            (recur (str result char)))))
      ch)))

(def file-path "ПУТЬ К ФАЙЛУ")

(def char-channel (file->char-channel file-path))
(def string-channel (char-channel->string-channel char-channel))

;; Чтение строк из канала
(loop [result (async/<! string-channel)]
  (when result
    (println result)
    (recur (async/<! string-channel))))
