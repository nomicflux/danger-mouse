# danger-mouse

Error handling for processing pipelines of information in Clojure.

The design 

For API documentation, see: https://nomicflux.github.io/danger-mouse/

## Usage

Capture errors for reporting while continuing to process data:
```clojure
(danger-mouse.catch-errors/catch-errors-> [1 2 3]
  (map inc)
  (map #(if (even? %) % (throw (Exception. (str %)))))
  (map #(* % 10)))

> {:result [20 40]
   :errors [{:error-msg "3"
             :input 2
             :error ....}]}
```

Use `try-catch` as a function, then `resolve` to apply different functions over
errors and successes, respectively:
```clojure
(->> (danger-mouse.macros/try-catch (throw (Exception. "Oops")))
     (danger-mouse.utils/resolve ex-message str))

> "Oops"

(->> (danger-mouse.macros/try-catch 1)
     (danger-mouse.utils/resolve ex-message str))

> "1"
```

Update transducers to capture errors, then handle the errors while passing on 
successful results:
```clojure
(let [collector (danger-mouse.transducers/collect 
                    (map inc)
                    (map #(if (even? %)
                              (danger-mouse.schema/as-error %)
                              %)))
                    (take 2)
                    (map (partial * 10)))]

    (->> (range 10)
         collector
         (danger-mouse.utils/handle-errors println)))

;; [2]
> [10 30]
```

## License

MIT License

Copyright © 2021-2023 Michael Anderson
  * [ ] 
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
