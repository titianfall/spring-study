package hello.core.discount;

import hello.core.member.Grade;
import hello.core.member.Member;

// 정액 할인 정책
public class FixDiscountPolicy implements DiscountPolicy{

    private int discountFixAmount = 1000;

    @Override
    public int discount(Member member, int price) {
        // 등급에 따른 할인 금액이 다름
        if (member.getGrade() == Grade.VIP) {
            return discountFixAmount;
        } else {
            return 0;
        }
    }
}
