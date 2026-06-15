package com.vasooli.radar.data

/**
 * Demo data so the app looks alive on first launch — a realistic spread of retailer
 * customers a small FMCG cash-&-carry wholesaler would carry: reliable payers,
 * shops sliding into trouble (declining orders = quick-commerce pressure), and
 * a couple of genuine high-risk accounts with old overdue and a broken promise.
 */
object SeedData {
    private const val DAY = 86_400_000L

    suspend fun seed(repo: Repository) {
        val now = System.currentTimeMillis()
        fun d(days: Int) = now - days * DAY

        suspend fun add(
            shop: String, owner: String, phone: String, area: String,
            limit: Double, term: Int, block: suspend (Long) -> Unit
        ) {
            val id = repo.addRetailer(
                Retailer(shopName = shop, ownerName = owner, phone = phone, area = area,
                    creditLimit = limit, termDays = term, createdAt = d(420))
            )
            block(id)
        }

        // one monthly buy + (optional) payment `payIn` days after the sale
        suspend fun cycle(id: Long, monthsBack: Int, amount: Double, term: Int, payIn: Int?) {
            val sd = d(monthsBack * 30)
            repo.addSale(id, amount, sd, sd + term * DAY, "Monthly stock")
            if (payIn != null) repo.addPayment(id, amount, sd + payIn * DAY, "Payment")
        }

        // 1 — SAFE: pays early, orders growing
        add("Sharma Kirana", "Ramesh Sharma", "9876500011", "Sector 21", 80000.0, 15) { id ->
            cycle(id, 6, 24000.0, 15, 11); cycle(id, 5, 26000.0, 15, 12); cycle(id, 4, 28000.0, 15, 10)
            cycle(id, 3, 30000.0, 15, 13); cycle(id, 2, 32000.0, 15, 12); cycle(id, 1, 34000.0, 15, 11)
            val sd = d(7); repo.addSale(id, 30000.0, sd, sd + 15 * DAY, "Festive stock") // current, not yet due
        }
        // 2 — SAFE: stable, pays within terms
        add("Gupta General Store", "Suresh Gupta", "9876500022", "Model Town", 50000.0, 21) { id ->
            cycle(id, 5, 18000.0, 21, 18); cycle(id, 4, 19000.0, 21, 20); cycle(id, 3, 17500.0, 21, 19)
            cycle(id, 2, 18500.0, 21, 17); cycle(id, 1, 19000.0, 21, 20)
            val sd = d(10); repo.addSale(id, 16000.0, sd, sd + 21 * DAY, "Stock")
        }
        // 3 — WATCH: chronically ~12 days late, near limit
        add("New Era Mart", "Vikas Jain", "9876500033", "Civil Lines", 60000.0, 15) { id ->
            cycle(id, 5, 40000.0, 15, 25); cycle(id, 4, 42000.0, 15, 28)
            cycle(id, 3, 38000.0, 15, 27); cycle(id, 2, 45000.0, 15, 30)
            val sd = d(20); repo.addSale(id, 50000.0, sd, sd + 15 * DAY, "Bulk order")
        }
        // 4 — WATCH→HIGH: orders collapsing (q-commerce nearby), going quiet
        add("Balaji Provision", "Anil Yadav", "9876500044", "Rohit Nagar", 40000.0, 15) { id ->
            cycle(id, 6, 30000.0, 15, 16); cycle(id, 5, 26000.0, 15, 18)
            cycle(id, 4, 20000.0, 15, 20); cycle(id, 3, 14000.0, 15, 22)
            val sd = d(40); repo.addSale(id, 9000.0, sd, sd + 15 * DAY, "Small order")
        }
        // 5 — HIGH: large old overdue + a broken promise
        add("Anand Traders", "Mahesh Anand", "9876500055", "Old Market", 70000.0, 15) { id ->
            cycle(id, 6, 35000.0, 15, 20); cycle(id, 5, 38000.0, 15, 30)
            val s1 = d(70); repo.addSale(id, 45000.0, s1, s1 + 15 * DAY, "Bulk")
            val s2 = d(50); repo.addSale(id, 30000.0, s2, s2 + 15 * DAY, "Bulk")
            repo.addPayment(id, 20000.0, d(30), "Part payment")
            repo.addPromise(id, 40000.0, d(10), d(25), "Promised to clear dues") // promised by 10 days ago, unpaid
        }
        // 6 — HIGH: over credit limit, very late, orders crashed
        add("Lucky Super Bazaar", "Deepak Verma", "9876500066", "Transport Nagar", 50000.0, 15) { id ->
            cycle(id, 6, 40000.0, 15, 35); cycle(id, 5, 30000.0, 15, 40)
            val s1 = d(80); repo.addSale(id, 35000.0, s1, s1 + 15 * DAY, "Stock")
            val s2 = d(60); repo.addSale(id, 30000.0, s2, s2 + 15 * DAY, "Stock")
        }
        // 7 — SAFE: small reliable shop
        add("Krishna Kirana", "Gopal Das", "9876500077", "Sector 9", 30000.0, 15) { id ->
            cycle(id, 4, 12000.0, 15, 12); cycle(id, 3, 13000.0, 15, 14)
            cycle(id, 2, 12500.0, 15, 13); cycle(id, 1, 13500.0, 15, 12)
            val sd = d(6); repo.addSale(id, 12000.0, sd, sd + 15 * DAY, "Stock")
        }
        // 8 — WATCH: erratic — sometimes very late, sits near limit
        add("Verma Stores", "Rakesh Verma", "9876500088", "Green Park", 35000.0, 20) { id ->
            cycle(id, 5, 22000.0, 20, 28); cycle(id, 4, 23000.0, 20, 19)
            cycle(id, 3, 24000.0, 20, 30); cycle(id, 2, 22000.0, 20, 18)
            val sd = d(25); repo.addSale(id, 26000.0, sd, sd + 20 * DAY, "Stock")
        }
    }
}
