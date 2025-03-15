namespace AiRise;

public class Program
{
    public static void Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);

        builder.Services.AddControllers();
        // FOR DEVELOPMENT API TESTING
        builder.Services.AddEndpointsApiExplorer();
        builder.Services.AddSwaggerGen(); 
        //
        var app = builder.Build();


        // Configure the HTTP request pipeline.
        // ONLY DURING DEVELOPMENT
        if (app.Environment.IsDevelopment()) 
        {
            app.UseSwagger(); 
            app.UseSwaggerUI(); 
        }

        app.UseHttpsRedirection();
        app.MapControllers();
        app.Run();
    }
}