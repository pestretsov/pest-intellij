<?php

test("myTest", function(int $a) {
    <warning descr="Failed asserting that 1 is identical to 2.">expect(1)->toBe($a);</warning>
    <warning descr="Failed asserting that 2 is identical to 1.">expect(2)->toBe($a);</warning>
})->with([[1, 1], [2, 2]]);