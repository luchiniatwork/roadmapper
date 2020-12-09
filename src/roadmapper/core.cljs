(ns roadmapper.core
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<! go]]
            [clojure.string :as s]))

(enable-console-print!)

(defmulti to-ms last)

(defmethod to-ms :days [[n & _]]
  (* n 24 60 60 1000))

(defmethod to-ms :day [[n & _]]
  (* n 24 60 60 1000))

(defmethod to-ms :weeks [[n & _]]
  (* 7 (to-ms [n :days])))

(defmethod to-ms :week [[n & _]]
  (* 7 (to-ms [n :days])))

(defmethod to-ms :months [[n & _]]
  (* 4 (to-ms [n :weeks])))

(defmethod to-ms :month [[n & _]]
  (* 4 (to-ms [n :weeks])))

(defmethod to-ms :quarters [[n & _]]
  (* 3 (to-ms [n :months])))

(defmethod to-ms :quarter [[n & _]]
  (* 3 (to-ms [n :months])))

(defn parse-effort [effort]
  (let [parts (s/split effort #" ")]
    [(-> parts first js/parseInt) (-> parts last keyword)]))

(defn set-columns [dt]
  (doto dt
    (.addColumn "string" "Task ID")
    (.addColumn "string" "Task Name")
    (.addColumn "string" "Resource")
    (.addColumn "date" "Start Date")
    (.addColumn "date" "End Date")
    (.addColumn "number" "Duration")
    (.addColumn "number" "Percent Complete")
    (.addColumn "string" "Dependencies")))

(defn fetch-data []
  (go
    (let [{:keys [status body]} (<! (http/get "roadmap.edn"))]
      (if (= 200 status)
        body
        (throw (ex-info "Can't find roadmap.edn file" {}))))))

(defn description [{:keys [description resource]}]
  (if resource
    (str description " (" resource ")")
    resource))

(defn dependencies [{:keys [follows]}]
  (s/join "," follows))

(defn duration [{:keys [effort]}]
  (-> effort parse-effort to-ms))

(defn format-data [{:keys [tasks]}]
  (map (fn [{:keys [id group start end completed]
             :as task}]
         [id (description task) group nil nil (duration task) 0 (dependencies task)])
       tasks))

(defn ^:export draw-chart []
  (go
    (let [chart (google.visualization.Gantt. (js/document.getElementById "chart_div"))
          data (<! (fetch-data))]
      (println (format-data data))
      (.draw chart
             (doto (google.visualization.DataTable.)
               set-columns
               (.addRows
                (clj->js (format-data data))))
             (clj->js {:height (* 55 (count (:tasks data)))})))))

(google.charts.load "current" (clj->js {:packages ["gantt"]}))
(google.charts.setOnLoadCallback draw-chart)
