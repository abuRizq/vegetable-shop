
import { Card, CategoryCard } from "./catagory-card";
const CARDS: Card[] = [
    {
        title: "Leafy Greens",
        emoji: "🥬",
        colorClass: "bg-primary",
        buttonClass: "btn-primary",
        description: "Fresh spinach, lettuce, kale and more",
    },
    {
        title: "Root Vegetables",
        emoji: "🥕",
        colorClass: "bg-secondary",
        buttonClass: "btn-secondary",
        description: "Carrots, potatoes, onions and more",
    },
    {
        title: "Fresh Produce",
        emoji: "🥒",
        colorClass: "bg-accent",
        buttonClass: "btn-outline",
        description: "Cucumbers, tomatoes, peppers and more",
    },
    {
        title: "Fruits",
        emoji: "🍎",
        colorClass: "bg-primary",
        buttonClass: "btn-primary",
        description: "Apples, bananas, berries and more",
    },
    {
        title: "Herbs",
        emoji: "🌿",
        colorClass: "bg-secondary",
        buttonClass: "btn-secondary",
        description: "Mint, basil, coriander and more",
    },
    {
        title: "Organic Boxes",
        emoji: "📦",
        colorClass: "bg-accent",
        buttonClass: "btn-outline",
        description: "Curated seasonal organic veggie boxes",
    },
];

const Category = () => {
    return (
        <section className="w-[92%] mx-auto mt-10">
            <h2
                className="text-3xl font-bold mb-6"
                style={{ color: "hsl(var(--text-primary))" }}
            >
                Fresh Categories
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {CARDS.map((c, i) => (
                    <CategoryCard key={c.title} c={c} idx={i} />
                ))}
            </div>
        </section>
    );
}
export { Category }