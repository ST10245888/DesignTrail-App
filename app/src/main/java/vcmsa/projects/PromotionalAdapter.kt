package vcmsa.projects.fkj_consultants.activities
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import vcmsa.projects.fkj_consultants.R

class PromotionalAdapter(
    private val context: android.content.Context,
    private val items: List<PromotionalItem>
) : ArrayAdapter<PromotionalItem>(context, 0, items) {

    override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
        val item = items[position]
        val view = convertView ?: android.view.LayoutInflater.from(context).inflate(R.layout.item_promotional, parent, false)

        val icon = view.findViewById<ImageView>(R.id.imgIcon)
        val name = view.findViewById<TextView>(R.id.txtName)
        val price = view.findViewById<TextView>(R.id.txtPrice)

        icon.setImageResource(item.iconResId)
        name.text = item.name
        price.text = "R ${item.pricePerUnit}"

        return view
    }
}
